package srpm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import srpm.dto.response.JiraGroupDto;
import srpm.dto.response.JiraUserDto;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;
import srpm.model.Student;
import srpm.repository.GroupRepository;
import srpm.repository.StudentRepository;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class JiraGroupSyncService {

    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(JiraGroupSyncService.class);

    @Autowired
    public JiraGroupSyncService(
            GroupRepository groupRepository,
            StudentRepository studentRepository,
            RestTemplate restTemplate
    ) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = restTemplate;
    }

    /**
     * Đồng bộ nhóm từ Jira sang hệ thống
     * @param groupId ID của group trong hệ thống
     * @param jiraGroupName Tên nhóm Jira (dùng làm Project Key)
     * @return JiraGroupDto chứa thông tin nhóm đã đồng bộ
     */
    @Transactional
    public JiraGroupDto syncJiraGroupToLocalGroup(Long groupId, String jiraGroupName) {
        // Lấy thông tin group từ database
        Group group = groupRepository.findByIdWithStudentsAndLecturer(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        // Kiểm tra cấu hình Jira
        if (group.getJiraUrl() == null || group.getJiraUrl().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình URL Jira cho group này");
        }
        if (group.getJiraApiToken() == null || group.getJiraApiToken().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình API token Jira cho group này");
        }
        if (group.getJiraAdminEmail() == null || group.getJiraAdminEmail().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình email admin Jira cho group này");
        }

        // Lấy assignable users từ Jira Project (Jira Cloud luôn dùng cách này)
        JiraGroupDto jiraGroup = getProjectAssignableUsers(
                group.getJiraUrl(),
                jiraGroupName,
                group.getJiraAdminEmail(),
                group.getJiraApiToken()
        );

        if (jiraGroup == null) {
            throw new RuntimeException("Không thể lấy thông tin nhóm từ Jira");
        }

        // Đồng bộ thành viên (Soft Sync - chỉ update thay đổi)
        syncGroupMembers(group, jiraGroup.getMembers() != null ? jiraGroup.getMembers() : new ArrayList<>());

        return jiraGroup;
    }

    /**
     * Lấy danh sách users assignable cho project (Jira Cloud - luồng chính)
     */
    private JiraGroupDto getProjectAssignableUsers(String jiraUrl, String projectKey, String adminEmail, String apiToken) {
        try {
            List<JiraUserDto> members = new ArrayList<>();
            String urlBase = jiraUrl.replaceAll("/$", "") + "/rest/api/3/user/assignable/search";
            String urlString = urlBase + "?project=" + projectKey + "&maxResults=50";

            logger.info("Fetching assignable users for Jira project: " + projectKey);

            // Chuẩn bị HTTP headers với Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = adminEmail + ":" + apiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Gọi Jira API bằng RestTemplate
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    urlString,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Jira API Response Code: " + response.getStatusCode().value());

                // Parse array of users
                List<Map<String, Object>> users = response.getBody();

                for (Map<String, Object> user : users) {
                    JiraUserDto jiraUser = new JiraUserDto(
                            (String) user.get("accountId"),
                            (String) user.get("emailAddress"),
                            (String) user.get("displayName"),
                            (Boolean) user.getOrDefault("active", true)
                    );
                    members.add(jiraUser);
                    logger.info("Found user: " + jiraUser.getDisplayName());
                }

                return new JiraGroupDto(projectKey, members);
            } else {
                String errorMsg = "Lỗi khi lấy users từ project: HTTP " + response.getStatusCode().value();
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy assignable users: " + e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy assignable users: " + e.getMessage(), e);
        }
    }

    /**
     * Soft Sync - Đồng bộ thành viên nhóm (chỉ update thay đổi, không xóa sạch)
     * - Xóa sinh viên không còn trong Jira
     * - Thêm sinh viên mới từ Jira
     * - Update thông tin sinh viên đã có
     */
    @Transactional
    protected void syncGroupMembers(Group group, List<JiraUserDto> jiraMembers) {
        Map<String, Long> jiraAccountIdToStudentId = new HashMap<>();
        List<Student> studentsToSave = new ArrayList<>();

        // Xử lý tất cả active Jira users
        for (JiraUserDto jiraUser : jiraMembers) {
            if (!jiraUser.isActive()) {
                continue;
            }

            Optional<Student> existingByAccountId = studentRepository.findByJiraAccountId(jiraUser.getAccountId());

            if (existingByAccountId.isPresent()) {
                // Update sinh viên đã tồn tại
                Student student = existingByAccountId.get();
                boolean changed = false;

                if (jiraUser.getEmailAddress() != null && !jiraUser.getEmailAddress().isEmpty()
                    && !jiraUser.getEmailAddress().equals(student.getEmail())) {
                    student.setEmail(jiraUser.getEmailAddress());
                    changed = true;
                }
                if (jiraUser.getDisplayName() != null && !jiraUser.getDisplayName().isEmpty()
                    && !jiraUser.getDisplayName().equals(student.getUsername())) {
                    student.setUsername(jiraUser.getDisplayName());
                    changed = true;
                }

                if (changed) {
                    studentsToSave.add(student);
                }
                jiraAccountIdToStudentId.put(jiraUser.getAccountId(), student.getID());
                logger.info("Updated student from Jira: " + student.getUsername());
            } else {
                // Tạo sinh viên mới
                Student newStudent = new Student();
                String email = jiraUser.getEmailAddress();
                if (email == null || email.isEmpty()) {
                    email = generateUniqueEmail(jiraUser.getAccountId());
                }
                newStudent.setEmail(email);
                newStudent.setUsername(jiraUser.getDisplayName() != null ? jiraUser.getDisplayName() : "User_" + jiraUser.getAccountId());
                newStudent.setJiraAccountId(jiraUser.getAccountId());
                newStudent.setPassword("temp_password");

                try {
                    studentsToSave.add(newStudent);
                    logger.info("Preparing to add new student from Jira: " + newStudent.getUsername());
                } catch (Exception e) {
                    logger.warn("Could not add student " + newStudent.getUsername() + ": " + e.getMessage());
                }
            }
        }

        // Batch save tất cả students (một lần thay vì trong loop)
        if (!studentsToSave.isEmpty()) {
            List<Student> savedStudents = studentRepository.saveAll(studentsToSave);
            for (Student student : savedStudents) {
                if (student.getJiraAccountId() != null && student.getID() != null) {
                    jiraAccountIdToStudentId.put(student.getJiraAccountId(), student.getID());
                }
            }
        }

        // Soft Sync: Remove sinh viên không còn trong Jira (thông qua GroupMember)
        Set<GroupMember> membersToRemove = new HashSet<>();
        for (GroupMember member : group.getGroupMembers()) {
            String jiraAccountId = member.getStudent().getJiraAccountId();
            if (jiraAccountId != null && !jiraAccountIdToStudentId.containsKey(jiraAccountId)) {
                membersToRemove.add(member);
                logger.info("Removing student from group (no longer in Jira): " + member.getStudent().getUsername());
            }
        }
        group.getGroupMembers().removeAll(membersToRemove);

        // Thêm sinh viên mới vào group thông qua GroupMember
        for (Map.Entry<String, Long> entry : jiraAccountIdToStudentId.entrySet()) {
            Long studentId = entry.getValue();
            Optional<Student> student = studentRepository.findById(studentId);
            if (student.isPresent()) {
                // Kiểm tra xem student đã có trong group không
                boolean alreadyInGroup = group.getGroupMembers().stream()
                        .anyMatch(gm -> gm.getStudent().getID().equals(studentId));
                if (!alreadyInGroup) {
                    GroupMember newMember = new GroupMember(group, student.get(), GroupMemberRole.TEAM_MEMBER);
                    group.getGroupMembers().add(newMember);
                    logger.info("Added student to group: " + student.get().getUsername());
                }
            }
        }

        // Lưu group
        groupRepository.save(group);
    }

    /**
     * Tạo email unique cho Jira user khi email từ Jira trống
     */
    private String generateUniqueEmail(String jiraAccountId) {
        long timestamp = System.currentTimeMillis();
        return "jira_" + jiraAccountId + "_" + timestamp + "@jira.local";
    }

    /**
     * Lấy danh sách nhóm từ Jira (deprecated - Jira Cloud không support)
     */
    public List<String> getJiraGroups(Long groupId) {
        logger.warn("getJiraGroups() is deprecated for Jira Cloud. Use project key directly.");
        return new ArrayList<>(); // Trả về empty list
    }
}

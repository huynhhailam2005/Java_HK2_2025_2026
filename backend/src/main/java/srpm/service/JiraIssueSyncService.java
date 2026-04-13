package srpm.service;

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
import srpm.model.Group;
import srpm.model.Issue;
import srpm.model.IssueType;
import srpm.model.IssueStatus;
import srpm.model.SyncStatus;
import srpm.repository.GroupRepository;
import srpm.repository.IssueRepository;
import srpm.repository.GroupMemberRepository;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class JiraIssueSyncService {

    private final GroupRepository groupRepository;
    private final IssueRepository issueRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(JiraIssueSyncService.class);

    @Autowired
    public JiraIssueSyncService(
            GroupRepository groupRepository,
            IssueRepository issueRepository,
            GroupMemberRepository groupMemberRepository,
            RestTemplate restTemplate
    ) {
        this.groupRepository = groupRepository;
        this.issueRepository = issueRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Map<String, Object> syncJiraIssuesToLocalIssues(Long groupId, String projectKey) {
        Group group = groupRepository.findByIdWithStudentsAndLecturer(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        if (group.getJiraUrl() == null || group.getJiraUrl().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình URL Jira cho group này");
        }
        if (group.getJiraApiToken() == null || group.getJiraApiToken().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình API token Jira cho group này");
        }
        if (group.getJiraAdminEmail() == null || group.getJiraAdminEmail().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình email admin Jira cho group này");
        }

        logger.info("Bắt đầu đồng bộ issues từ Jira project: {}", projectKey);

        // Fetch tất cả issues từ Jira bằng POST method để an toàn và lấy đầy đủ fields
        List<Map<String, Object>> jiraIssues = fetchAllIssuesFromJira(
                group.getJiraUrl(),
                projectKey,
                group.getJiraAdminEmail(),
                group.getJiraApiToken()
        );

        // Sync theo thứ tự ưu tiên: Epic -> Story/Task -> Sub-Task để không bị mất Parent
        Map<String, Issue> syncedIssuesMap = syncIssuesInOrder(group, jiraIssues);

        return Map.of(
                "success", true,
                "totalIssues", syncedIssuesMap.size(),
                "message", "Đã đồng bộ thành công " + syncedIssuesMap.size() + " issues từ Jira"
        );
    }

    private List<Map<String, Object>> fetchAllIssuesFromJira(String jiraUrl, String projectKey, String adminEmail, String apiToken) {
        try {
            // Dùng endpoint mới của Jira Cloud
            String urlBase = jiraUrl.replaceAll("/$", "") + "/rest/api/3/search/jql";

            // JQL lấy toàn bộ issues của Project
            String jql = "project = \"" + projectKey + "\"";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jql);
            requestBody.put("maxResults", 1000);
            // Lấy "*all" để đảm bảo hút được Epic Link và Assignee đầy đủ
            requestBody.put("fields", new String[]{"*all"});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = adminEmail + ":" + apiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    urlBase,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> issues = (List<Map<String, Object>>) response.getBody().get("issues");
                logger.info("Đã hút về {} issues từ Jira API", issues != null ? issues.size() : 0);
                return issues != null ? issues : new ArrayList<>();
            } else {
                throw new RuntimeException("Lỗi HTTP " + response.getStatusCode().value() + " khi gọi Jira");
            }
        } catch (Exception e) {
            logger.error("Lỗi khi fetch issues từ Jira: " + e.getMessage());
            throw new RuntimeException("Lỗi đồng bộ Jira: " + e.getMessage());
        }
    }

    @Transactional
    protected Map<String, Issue> syncIssuesInOrder(Group group, List<Map<String, Object>> jiraIssues) {
        Map<String, Issue> syncedIssuesMap = new HashMap<>();
        Map<String, IssueType> localTypeMap = new HashMap<>();

        // Lấy danh sách các Jira Keys vừa hút về
        List<String> jiraKeys = new ArrayList<>();

        // Bước 0: Phân loại Type trước cho tất cả để chạy cho nhanh
        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            jiraKeys.add(key);
            localTypeMap.put(key, determineIssueType(jiraIssue));
        }

        // Bước 1: Đồng bộ EPIC trước
        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            if (localTypeMap.get(key) == IssueType.EPIC) {
                Issue localIssue = syncSingleIssue(group, jiraIssue, null, IssueType.EPIC);
                syncedIssuesMap.put(key, localIssue);
            }
        }

        // Bước 2: Đồng bộ STORY / TASK / BUG (Có thể có cha là Epic)
        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            IssueType type = localTypeMap.get(key);
            if (type == IssueType.STORY || type == IssueType.TASK || type == IssueType.BUG) {
                String parentKey = getParentKey(jiraIssue);
                Issue parentIssue = (parentKey != null) ? syncedIssuesMap.get(parentKey) : null;

                Issue localIssue = syncSingleIssue(group, jiraIssue, parentIssue, type);
                syncedIssuesMap.put(key, localIssue);
            }
        }

        // Bước 3: Đồng bộ SUB-TASK (Bắt buộc phải có Task/Story cha)
        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            if (localTypeMap.get(key) == IssueType.SUB_TASK) {
                String parentKey = getParentKey(jiraIssue);
                Issue parentIssue = (parentKey != null) ? syncedIssuesMap.get(parentKey) : null;

                if (parentIssue != null) {
                    Issue localIssue = syncSingleIssue(group, jiraIssue, parentIssue, IssueType.SUB_TASK);
                    syncedIssuesMap.put(key, localIssue);
                } else {
                    logger.warn("Bỏ qua Sub-task {} vì không tìm thấy issue cha {}", key, parentKey);
                }
            }
        }

        // Bước 4: Xử lý XÓA từ phía Jira
        // Tìm các Issue trong Database SRPM thuộc group này, có issueCode, nhưng KHÔNG nằm trong danh sách vừa hút về từ Jira
        // => Điều này có nghĩa là ai đó đã xóa trên Jira
        logger.info("Kiểm tra các Issue bị xóa từ phía Jira...");
        List<Issue> deletedOnJira = issueRepository.findIssuesNotInJiraKeys(group.getId(), jiraKeys);
        for (Issue deletedIssue : deletedOnJira) {
            logger.warn("Phát hiện Issue {} bị xóa từ phía Jira, đánh dấu Soft-Delete trên SRPM", deletedIssue.getIssueCode());
            deletedIssue.setIsDeleted(true);
            deletedIssue.setSyncStatus(SyncStatus.SYNCED);
            issueRepository.save(deletedIssue);
        }

        return syncedIssuesMap;
    }

    @Transactional
    protected Issue syncSingleIssue(Group group, Map<String, Object> jiraIssue, Issue parentIssue, IssueType issueType) {
        String issueKey = (String) jiraIssue.get("key");
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) jiraIssue.get("fields");

        Optional<Issue> existing = issueRepository.findByIssueCodeAndGroupId(issueKey, group.getId());
        Issue localIssue = existing.orElse(new Issue());

        localIssue.setIssueCode(issueKey);
        localIssue.setGroup(group);
        localIssue.setTitle((String) fields.get("summary"));
        localIssue.setDescription(fields.get("description") instanceof String ? (String) fields.get("description") : "");
        localIssue.setIssueType(issueType);

        if (parentIssue != null) {
            localIssue.setParent(parentIssue);
        }

        // --- ĐỒNG BỘ NGƯỜI ĐƯỢC GIAO (ASSIGNEE) ĐÃ TỐI ƯU ---
        @SuppressWarnings("unchecked")
        Map<String, Object> assignee = (Map<String, Object>) fields.get("assignee");
        if (assignee != null) {
            String accountId = (String) assignee.get("accountId");
            String emailAddress = (String) assignee.get("emailAddress");
            String displayName = (String) assignee.get("displayName");

            logger.info("Issue {} has assignee - accountId: {}, email: {}, displayName: {}", issueKey, accountId, emailAddress, displayName);

            if (accountId != null) {
                // Tìm trực tiếp GroupMember của group hiện tại có jiraAccountId match
                logger.info("Tìm GroupMember trong group {} có jiraAccountId: {}", group.getId(), accountId);
                var memberOpt = groupMemberRepository.findByGroupAndJiraAccountId(group.getId(), accountId);

                if (memberOpt.isPresent()) {
                    localIssue.setAssignedTo(memberOpt.get());
                    logger.info("Assigned issue {} to GroupMember {} (jiraAccountId: {})",
                        issueKey, memberOpt.get().getId(), accountId);
                } else {
                    logger.warn("Không tìm thấy GroupMember trong group {} với jiraAccountId: {}",
                        group.getId(), accountId);
                }
            }
        } else {
            logger.debug("Issue {} không có assignee", issueKey);
            localIssue.setAssignedTo(null);
        }
        // ----------------------------------------------------

        if (existing.isEmpty()) {
            localIssue.setStatus(IssueStatus.TODO); // Trạng thái mặc định cho task mới
        } else {
            // ========== ĐỒNG BỘ TRẠNG THÁI TỪ JIRA ==========
            // Lấy status từ Jira và map về IssueStatus của hệ thống
            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) fields.get("status");
            if (status != null) {
                String jiraStatusName = (String) status.get("name");
                IssueStatus mappedStatus = mapJiraStatusToLocal(jiraStatusName);
                localIssue.setStatus(mappedStatus);
                logger.info("✓ Đồng bộ status từ Jira: {} -> {}", jiraStatusName, mappedStatus);
            }
        }

        // Luôn đánh dấu là SYNCED sau khi pull từ Jira, và isDeleted = false (vì đang tồn tại trên Jira)
        localIssue.setSyncStatus(SyncStatus.SYNCED);
        localIssue.setIsDeleted(false);

        return issueRepository.save(localIssue);
    }

    private String getParentKey(Map<String, Object> jiraIssue) {
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) jiraIssue.get("fields");
        if (fields == null) return null;

        // Ưu tiên 1: Trường "parent" chuẩn của Jira Cloud hiện đại
        @SuppressWarnings("unchecked")
        Map<String, Object> parent = (Map<String, Object>) fields.get("parent");
        if (parent != null) {
            return (String) parent.get("key");
        }

        // Ưu tiên 2: Quét Epic Link trong các custom fields (dành cho Jira đời cũ)
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (entry.getKey().startsWith("customfield_") && entry.getValue() instanceof String val) {
                if (val.matches("^[A-Z][A-Z0-9]+-[0-9]+$")) {
                    return val;
                }
            }
        }
        return null;
    }

    private IssueType determineIssueType(Map<String, Object> jiraIssue) {
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) jiraIssue.get("fields");
        if (fields == null) return IssueType.TASK;

        @SuppressWarnings("unchecked")
        Map<String, Object> it = (Map<String, Object>) fields.get("issuetype");
        if (it == null) return IssueType.TASK;

        // 1. Check cờ subtask của Jira (Chuẩn nhất, bất chấp ngôn ngữ)
        if (Boolean.TRUE.equals(it.get("subtask"))) {
            return IssueType.SUB_TASK;
        }

        // 2. Check HierarchyLevel (0 = Task/Story, 1 = Epic)
        Number level = (Number) it.get("hierarchyLevel");
        if (level != null && level.intValue() == 1) return IssueType.EPIC;

        // 3. Fallback bằng tên nếu các cờ trên không tồn tại
        String name = ((String) it.get("name")).toUpperCase();
        if (name.contains("EPIC")) return IssueType.EPIC;
        if (name.contains("STORY") || name.contains("CÂU CHUYỆN")) return IssueType.STORY;
        if (name.contains("BUG") || name.contains("LỖI")) return IssueType.BUG;

        return IssueType.TASK;
    }

    /**
     * Map Jira Status sang IssueStatus của hệ thống
     * Jira có các status chuẩn: To Do, In Progress, Done
     * Hệ thống có: TODO, IN_PROGRESS, DONE, CANCELLED
     */
    private IssueStatus mapJiraStatusToLocal(String jiraStatusName) {
        if (jiraStatusName == null) {
            return IssueStatus.TODO;
        }

        String status = jiraStatusName.toUpperCase();

        // Mapping chuẩn Jira
        if (status.contains("DONE") || status.contains("CLOSED")) {
            return IssueStatus.DONE;
        }
        if (status.contains("IN PROGRESS") || status.contains("INPROGRESS")) {
            return IssueStatus.IN_PROGRESS;
        }
        if (status.contains("CANCELLED") || status.contains("CANCEL") || status.contains("REJECTED")) {
            return IssueStatus.CANCELLED;
        }
        if (status.contains("TO DO") || status.contains("TODO") || status.contains("BACKLOG") || status.contains("OPEN")) {
            return IssueStatus.TODO;
        }

        // Default
        return IssueStatus.TODO;
    }
}
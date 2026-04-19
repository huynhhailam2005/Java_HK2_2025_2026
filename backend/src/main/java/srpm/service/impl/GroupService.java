package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.repository.GroupMemberRepository;
import srpm.repository.GroupRepository;
import srpm.repository.LecturerRepository;
import srpm.repository.StudentRepository;
import srpm.service.IGroupService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GroupService implements IGroupService {

    private final GroupRepository groupDao;
    private final LecturerRepository lecturerDao;
    private final StudentRepository studentDao;
    private final GroupMemberRepository groupMemberDao;

    @Autowired
    public GroupService(
            GroupRepository groupDao,
            LecturerRepository lecturerDao,
            StudentRepository studentDao,
            GroupMemberRepository groupMemberDao
    ) {
        this.groupDao = groupDao;
        this.lecturerDao = lecturerDao;
        this.studentDao = studentDao;
        this.groupMemberDao = groupMemberDao;
    }

    /** Lấy tất cả group */
    @Transactional
    public List<Group> getAllGroups() {
        return groupDao.findAllWithStudentsAndLecturer();
    }

    /** Lấy group theo id */
    @Transactional
    public Optional<Group> getGroupById(Long id) {
        return groupDao.findByIdWithStudentsAndLecturer(id);
    }

    /** Tạo group mới */
    @Transactional
    public Group createGroup(GroupRequest req) {
        // Kiểm tra mã group trùng
        if (groupDao.existsByGroupCode(req.getGroupCode())) {
            throw new RuntimeException("Mã group '" + req.getGroupCode() + "' đã tồn tại");
        }

        // Kiểm tra tên group trùng
        if (groupDao.existsByGroupName(req.getGroupName())) {
            throw new RuntimeException("Tên group '" + req.getGroupName() + "' đã tồn tại");
        }

        // Kiểm tra Jira URL + Project Key trùng
        if (req.getJiraUrl() != null && req.getJiraProjectKey() != null) {
            if (groupDao.existsByJiraUrlAndProjectKey(req.getJiraUrl(), req.getJiraProjectKey())) {
                throw new RuntimeException("Jira URL + Project Key này đã được sử dụng cho group khác");
            }
        }

        Lecturer lecturer = lecturerDao.findByUserId(req.getLecturerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + req.getLecturerId()));

        Group group = new Group(req.getGroupCode(), req.getGroupName(), lecturer);
        group.setCreatedAt(LocalDateTime.now());

        if (req.getJiraUrl() != null) group.setJiraUrl(req.getJiraUrl());
        if (req.getJiraProjectKey() != null) group.setJiraProjectKey(req.getJiraProjectKey());
        if (req.getJiraApiToken() != null) group.setJiraApiToken(req.getJiraApiToken());
        if (req.getJiraAdminEmail() != null) group.setJiraAdminEmail(req.getJiraAdminEmail());
        if (req.getGithubRepoUrl() != null) group.setGithubRepoUrl(req.getGithubRepoUrl());
        if (req.getGithubAccessToken() != null) group.setGithubAccessToken(req.getGithubAccessToken());

        return groupDao.save(group);
    }

    /** Cập nhật tên group */
    @Transactional
    public Optional<Group> updateGroupName(Long id, String newName) {
        return groupDao.findByIdWithStudentsAndLecturer(id).map(group -> {
            if (newName != null && !newName.trim().isEmpty()) {
                group.setGroupName(newName);
                return groupDao.save(group);
            }
            return group;
        });
    }

    /** Cập nhật giảng viên cho group */
    @Transactional
    public Optional<Group> updateGroupLecturer(Long id, Long lecturerId) {
        return groupDao.findByIdWithStudentsAndLecturer(id).map(group -> {
            if (lecturerId != null) {
                Lecturer lecturer = lecturerDao.findByUserId(lecturerId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + lecturerId));
                group.setLecturer(lecturer);
                return groupDao.save(group);
            }
            return group;
        });
    }

    /** Cập nhật toàn bộ thông tin group */
    @Transactional
    public Optional<Group> updateGroupInfo(Long id, UpdateGroupRequest request) {
        return groupDao.findByIdWithStudentsAndLecturer(id).map(group -> {
            // Cập nhật tên group - kiểm tra trùng
            if (request.getGroupName() != null && !request.getGroupName().trim().isEmpty()) {
                if (!request.getGroupName().equals(group.getGroupName()) && 
                    groupDao.existsByGroupNameAndIdNot(request.getGroupName(), id)) {
                    throw new RuntimeException("Tên group '" + request.getGroupName() + "' đã tồn tại");
                }
                group.setGroupName(request.getGroupName());
            }

            // Cập nhật giảng viên
            if (request.getLecturerId() != null) {
                Lecturer lecturer = lecturerDao.findByUserId(request.getLecturerId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + request.getLecturerId()));
                group.setLecturer(lecturer);
            }

            // Kiểm tra Jira URL + Project Key trùng trước khi update
            if (request.getJiraUrl() != null || request.getJiraProjectKey() != null) {
                String newJiraUrl = request.getJiraUrl() != null ? request.getJiraUrl() : group.getJiraUrl();
                String newProjectKey = request.getJiraProjectKey() != null ? request.getJiraProjectKey() : group.getJiraProjectKey();
                
                if (newJiraUrl != null && newProjectKey != null) {
                    // Nếu URL hoặc Key thay đổi, kiểm tra xem có trùng với group khác không
                    if ((!newJiraUrl.equals(group.getJiraUrl()) || !newProjectKey.equals(group.getJiraProjectKey())) &&
                        groupDao.existsByJiraUrlAndProjectKeyAndIdNot(newJiraUrl, newProjectKey, id)) {
                        throw new RuntimeException("Jira URL + Project Key này đã được sử dụng cho group khác");
                    }
                }
            }

            // Cập nhật Jira thông tin
            if (request.getJiraUrl() != null) {
                group.setJiraUrl(request.getJiraUrl());
            }
            if (request.getJiraProjectKey() != null) {
                group.setJiraProjectKey(request.getJiraProjectKey());
            }
            if (request.getJiraApiToken() != null) {
                group.setJiraApiToken(request.getJiraApiToken());
            }
            if (request.getJiraAdminEmail() != null) {
                group.setJiraAdminEmail(request.getJiraAdminEmail());
            }

            // Cập nhật GitHub thông tin
            if (request.getGithubRepoUrl() != null) {
                group.setGithubRepoUrl(request.getGithubRepoUrl());
            }
            if (request.getGithubAccessToken() != null) {
                group.setGithubAccessToken(request.getGithubAccessToken());
            }

            return groupDao.save(group);
        });
    }


    /** Xóa group */
    @Transactional
    public boolean deleteGroup(Long id) {
        if (!groupDao.existsById(id)) return false;
        groupDao.deleteById(id);
        return true;
    }

    /** Thêm student vào group */
    @Transactional
    public Group addStudent(Long groupId, Long studentId) {
        try {
            Group group = groupDao.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

            Student student = studentDao.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy student: " + studentId));

            if (groupMemberDao.findByGroupAndStudent(groupId, studentId).isPresent()) {
                throw new RuntimeException("Student đã có trong group");
            }

            GroupMember member = new GroupMember(group, student, GroupMemberRole.TEAM_MEMBER);
            groupMemberDao.save(member);

            // Refresh from database after saving
            return groupDao.findByIdWithStudentsAndLecturer(groupId)
                    .orElseThrow(() -> new RuntimeException("Không thể tải lại group sau khi thêm student"));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi thêm student vào group: " + e.getMessage(), e);
        }
    }

    /** Xóa student khỏi group */
    @Transactional
    public boolean removeStudent(Long groupId, Long studentId) {
        Optional<GroupMember> memberOptional = groupMemberDao.findByGroupAndStudent(groupId, studentId);

        if (memberOptional.isPresent()) {
            groupMemberDao.delete(memberOptional.get());
            return true;
        }
        return false;
    }

    public List<Group> getGroupsByLecturer(Long lecturerId) {
        return groupDao.findByLecturerId(lecturerId);
    }

    public List<Group> getGroupsByStudent(Long studentId) {
        return groupMemberDao.findGroupsByStudentId(studentId);
    }
}

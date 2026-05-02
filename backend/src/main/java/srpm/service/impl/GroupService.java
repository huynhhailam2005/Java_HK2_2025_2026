package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.GroupDto;
import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IGroupRepository;
import srpm.repository.ILecturerRepository;
import srpm.repository.IStudentRepository;
import srpm.service.IGroupService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GroupService implements IGroupService {

    private final IGroupRepository groupDao;
    private final ILecturerRepository lecturerDao;
    private final IStudentRepository studentDao;
    private final IGroupMemberRepository groupMemberDao;

    @Autowired
    public GroupService(
            IGroupRepository groupDao,
            ILecturerRepository lecturerDao,
            IStudentRepository studentDao,
            IGroupMemberRepository groupMemberDao
    ) {
        this.groupDao = groupDao;
        this.lecturerDao = lecturerDao;
        this.studentDao = studentDao;
        this.groupMemberDao = groupMemberDao;
    }

    @Transactional
    public List<GroupDto> getAllGroups() {
        return groupDao.findAllWithStudentsAndLecturer()
                .stream()
                .map(GroupDto::fromEntity)
                .toList();
    }

    @Transactional
    public Optional<GroupDto> getGroupById(Long id) {
        return groupDao.findByIdWithStudentsAndLecturer(id)
                .map(GroupDto::fromEntity);
    }

    @Transactional
    public GroupDto createGroup(GroupRequest req) {
        if (groupDao.existsByGroupCode(req.getGroupCode())) {
            throw new RuntimeException("Mã group '" + req.getGroupCode() + "' đã tồn tại");
        }

        if (groupDao.existsByGroupName(req.getGroupName())) {
            throw new RuntimeException("Tên group '" + req.getGroupName() + "' đã tồn tại");
        }

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

        return GroupDto.fromEntity(groupDao.save(group));
    }

    @Transactional
    public Optional<GroupDto> updateGroupName(Long id, String newName) {
        return groupDao.findByIdWithStudentsAndLecturer(id).map(group -> {
            if (newName != null && !newName.trim().isEmpty()) {
                group.setGroupName(newName);
                return GroupDto.fromEntity(groupDao.save(group));
            }
            return GroupDto.fromEntity(group);
        });
    }

    @Transactional
    public Optional<GroupDto> updateGroupLecturer(Long id, Long lecturerId) {
        return groupDao.findByIdWithStudentsAndLecturer(id).map(group -> {
            if (lecturerId != null) {
                Lecturer lecturer = lecturerDao.findByUserId(lecturerId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + lecturerId));
                group.setLecturer(lecturer);
                return GroupDto.fromEntity(groupDao.save(group));
            }
            return GroupDto.fromEntity(group);
        });
    }

    @Transactional
    public Optional<GroupDto> updateGroupInfo(Long id, UpdateGroupRequest request) {
        return groupDao.findByIdWithStudentsAndLecturer(id).map(group -> {
            // Cập nhật tên group - kiểm tra trùng
            if (request.getGroupName() != null && !request.getGroupName().trim().isEmpty()) {
                if (!request.getGroupName().equals(group.getGroupName()) && 
                    groupDao.existsByGroupNameAndIdNot(request.getGroupName(), id)) {
                    throw new RuntimeException("Tên group '" + request.getGroupName() + "' đã tồn tại");
                }
                group.setGroupName(request.getGroupName());
            }

            if (request.getLecturerId() != null) {
                Lecturer lecturer = lecturerDao.findByUserId(request.getLecturerId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên: " + request.getLecturerId()));
                group.setLecturer(lecturer);
            }

            if (request.getJiraUrl() != null || request.getJiraProjectKey() != null) {
                String newJiraUrl = request.getJiraUrl() != null ? request.getJiraUrl() : group.getJiraUrl();
                String newProjectKey = request.getJiraProjectKey() != null ? request.getJiraProjectKey() : group.getJiraProjectKey();
                
                if (newJiraUrl != null && newProjectKey != null) {
                    if ((!newJiraUrl.equals(group.getJiraUrl()) || !newProjectKey.equals(group.getJiraProjectKey())) &&
                        groupDao.existsByJiraUrlAndProjectKeyAndIdNot(newJiraUrl, newProjectKey, id)) {
                        throw new RuntimeException("Jira URL + Project Key này đã được sử dụng cho group khác");
                    }
                }
            }

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

            if (request.getGithubRepoUrl() != null) {
                group.setGithubRepoUrl(request.getGithubRepoUrl());
            }
            if (request.getGithubAccessToken() != null) {
                group.setGithubAccessToken(request.getGithubAccessToken());
            }

            return GroupDto.fromEntity(groupDao.save(group));
        });
    }


    @Transactional
    public boolean deleteGroup(Long id) {
        if (!groupDao.existsById(id)) return false;
        groupDao.deleteById(id);
        return true;
    }

    @Transactional
    public GroupDto addStudent(Long groupId, Long studentId) {
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

            Group refreshedGroup = groupDao.findByIdWithStudentsAndLecturer(groupId)
                    .orElseThrow(() -> new RuntimeException("Không thể tải lại group sau khi thêm student"));
            return GroupDto.fromEntity(refreshedGroup);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi thêm student vào group: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean removeStudent(Long groupId, Long studentId) {
        Optional<GroupMember> memberOptional = groupMemberDao.findByGroupAndStudent(groupId, studentId);

        if (memberOptional.isPresent()) {
            groupMemberDao.delete(memberOptional.get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean removeMember(Long groupId, Long memberId) {
        if (!groupDao.existsById(groupId)) {
            throw new RuntimeException("Nhóm không tồn tại: " + groupId);
        }

        Optional<GroupMember> memberOptional = groupMemberDao.findById(memberId);
        if (memberOptional.isEmpty()) {
            throw new RuntimeException("Thành viên không tồn tại: " + memberId);
        }

        GroupMember member = memberOptional.get();

        if (!member.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Thành viên không thuộc nhóm này");
        }

        groupMemberDao.delete(member);
        return true;
    }

    public List<GroupDto> getGroupsByLecturer(Long lecturerId) {
        return groupDao.findByLecturerId(lecturerId)
                .stream()
                .map(GroupDto::fromEntity)
                .toList();
    }

    public List<GroupDto> getGroupsByStudent(Long studentId) {
        return groupMemberDao.findGroupsByStudentId(studentId)
                .stream()
                .map(GroupDto::fromEntity)
                .toList();
    }
}

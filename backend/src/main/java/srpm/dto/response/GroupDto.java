package srpm.dto.response;

import srpm.model.Group;
import srpm.model.Student;
import srpm.model.GroupMember;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupDto {
    private Long id;
    private String groupId;
    private String groupName;
    private Long lecturerId;
    private String lecturerUsername;
    private List<Long> studentIds;
    private LocalDateTime createdAt;

    public GroupDto() {}

    public GroupDto(Long id, String groupId, String groupName, Long lecturerId, String lecturerUsername, List<Long> studentIds, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.lecturerId = lecturerId;
        this.lecturerUsername = lecturerUsername;
        this.studentIds = studentIds;
        this.createdAt = createdAt;
    }

    public static GroupDto fromEntity(Group group) {
        List<Long> ids = new ArrayList<>();

        // Lấy danh sách student IDs từ GroupMembers
        Set<GroupMember> groupMembers = group.getGroupMembers();
        if (groupMembers != null && !groupMembers.isEmpty()) {
            ids = groupMembers.stream()
                    .map(gm -> gm.getStudent().getID())
                    .toList();
        }

        Long resolvedLecturerId = group.getLecturer() == null ? null : group.getLecturer().getID();
        String resolvedLecturerUsername = group.getLecturer() == null ? null : group.getLecturer().getUsername();

        return new GroupDto(
                group.getId(),
                group.getGroupCode(),
                group.getGroupName(),
                resolvedLecturerId,
                resolvedLecturerUsername,
                ids,
                group.getCreatedAt()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Long getLecturerId() { return lecturerId; }
    public void setLecturerId(Long lecturerId) { this.lecturerId = lecturerId; }
    public String getLecturerUsername() { return lecturerUsername; }
    public void setLecturerUsername(String lecturerUsername) { this.lecturerUsername = lecturerUsername; }
    public List<Long> getStudentIds() { return studentIds; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package srpm.dto;

import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;

import java.time.LocalDateTime;

public class GroupMemberDto {
    private Long id;
    private Long groupId;
    private Long studentId;
    private String studentCode;
    private String studentUsername;
    private GroupMemberRole memberRole;
    private LocalDateTime joinedAt;

    public GroupMemberDto() {}

    public GroupMemberDto(Long id, Long groupId, Long studentId, String studentCode, String studentUsername,
                          GroupMemberRole memberRole, LocalDateTime joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentUsername = studentUsername;
        this.memberRole = memberRole;
        this.joinedAt = joinedAt;
    }

    public static GroupMemberDto fromEntity(GroupMember member) {
        return new GroupMemberDto(
                member.getId(),
                member.getGroup().getId(),
                member.getStudent().getId(),
                member.getStudent().getStudentCode(),
                member.getStudent().getUsername(),
                member.getGroupMemberRole(),
                member.getJoinedAt()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }
    public GroupMemberRole getMemberRole() { return memberRole; }
    public void setMemberRole(GroupMemberRole memberRole) { this.memberRole = memberRole; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}


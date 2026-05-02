package srpm.dto;

import java.time.LocalDateTime;

public class MemberInfoDto {
    private Long id;
    private Long groupMemberId; // ID của bảng group_members (dùng để gán Issue)
    private String username;
    private String studentCode;
    private String email;
    private String role;
    private LocalDateTime joinedAt;

    public MemberInfoDto() {}

    public MemberInfoDto(Long id, Long groupMemberId, String username, String studentCode, String email, String role,
                         LocalDateTime joinedAt) {
        this.id = id;
        this.groupMemberId = groupMemberId;
        this.username = username;
        this.studentCode = studentCode;
        this.email = email;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public Long getGroupMemberId() { return groupMemberId; }
    public String getUsername() { return username; }
    public String getStudentCode() { return studentCode; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }

    public void setId(Long id) { this.id = id; }
    public void setGroupMemberId(Long groupMemberId) { this.groupMemberId = groupMemberId; }
    public void setUsername(String username) { this.username = username; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}


package srpm.dto.response;

import srpm.model.Role;

public class AdminUserManagementResponse {
    private String id;
    private String username;
    private String email;
    private Role role;
    private String studentId;

    public AdminUserManagementResponse() {}

    public AdminUserManagementResponse(String id, String username, String email, Role role, String studentId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.studentId = studentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}


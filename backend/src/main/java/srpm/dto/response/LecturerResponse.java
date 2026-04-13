package srpm.dto.response;

import srpm.model.UserRole;

public class LecturerResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole userRole;
    private String lecturerCode;

    public LecturerResponse() {}

    public LecturerResponse(Long id, String username, String email, UserRole userRole, String lecturerCode) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
        this.lecturerCode = lecturerCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getLecturerCode() {
        return lecturerCode;
    }

    public void setLecturerCode(String lecturerCode) {
        this.lecturerCode = lecturerCode;
    }
}


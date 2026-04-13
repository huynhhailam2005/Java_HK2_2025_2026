package srpm.dto.response;

import srpm.model.UserRole;

public class AdminUserManagementResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole userRole;
    private String studentCode;
    private String lecturerCode;
    private String jiraAccountId;
    private String githubUsername;

    public AdminUserManagementResponse() {}

    public AdminUserManagementResponse(Long id, String username, String email, UserRole userRole) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
    }

    public AdminUserManagementResponse(Long id, String username, String email, UserRole userRole,
                                       String studentCode, String lecturerCode,
                                       String jiraAccountId, String githubUsername) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
        this.studentCode = studentCode;
        this.lecturerCode = lecturerCode;
        this.jiraAccountId = jiraAccountId;
        this.githubUsername = githubUsername;
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

    public UserRole getRole() {
        return userRole;
    }

    public void setRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getLecturerCode() {
        return lecturerCode;
    }

    public void setLecturerCode(String lecturerCode) {
        this.lecturerCode = lecturerCode;
    }

    public String getJiraAccountId() {
        return jiraAccountId;
    }

    public void setJiraAccountId(String jiraAccountId) {
        this.jiraAccountId = jiraAccountId;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }
}

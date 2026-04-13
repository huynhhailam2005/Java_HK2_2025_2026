package srpm.dto.response;

import srpm.model.UserRole;

public class StudentResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole userRole;
    private String studentCode;
    private String jiraAccountId;
    private String githubUsername;

    public StudentResponse() {}

    public StudentResponse(Long id, String username, String email, UserRole userRole, String studentCode) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
        this.studentCode = studentCode;
    }

    public StudentResponse(Long id, String username, String email, UserRole userRole, String studentCode,
                          String jiraAccountId, String githubUsername) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
        this.studentCode = studentCode;
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

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
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


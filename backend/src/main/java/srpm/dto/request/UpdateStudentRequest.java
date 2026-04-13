package srpm.dto.request;

public class UpdateStudentRequest {
    private String username;
    private String email;
    private String password;
    private String studentId;
    private String jiraAccountId;
    private String githubUsername;

    public UpdateStudentRequest() {}

    public UpdateStudentRequest(String username, String email, String password, String studentId,
                              String jiraAccountId, String githubUsername) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.jiraAccountId = jiraAccountId;
        this.githubUsername = githubUsername;
    }

    public String getUsername() { return this.username; }
    public String getEmail() { return this.email; }
    public String getPassword() { return this.password; }
    public String getStudentId() { return this.studentId; }
    public String getJiraAccountId() { return this.jiraAccountId; }
    public String getGithubUsername() { return this.githubUsername; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setJiraAccountId(String jiraAccountId) { this.jiraAccountId = jiraAccountId; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }
}


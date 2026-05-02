package srpm.dto;

public class StudentSearchDto {
    private Long id;
    private String username;
    private String studentCode;
    private String email;
    private String githubUsername;
    private String jiraAccountId;

    public StudentSearchDto() {}

    public StudentSearchDto(Long id, String username, String studentCode, String email,
                            String githubUsername, String jiraAccountId) {
        this.id = id;
        this.username = username;
        this.studentCode = studentCode;
        this.email = email;
        this.githubUsername = githubUsername;
        this.jiraAccountId = jiraAccountId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }
    public String getJiraAccountId() { return jiraAccountId; }
    public void setJiraAccountId(String jiraAccountId) { this.jiraAccountId = jiraAccountId; }
}

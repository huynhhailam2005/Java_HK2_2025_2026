package srpm.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateGroupRequest {
    @NotBlank(message = "Group name không được để trống")
    private String groupName;

    private Long lecturerId;
    private String jiraUrl;
    private String jiraProjectKey;
    private String jiraApiToken;
    private String jiraAdminEmail;
    private String githubRepoUrl;
    private String githubAccessToken;

    public UpdateGroupRequest() {}

    public UpdateGroupRequest(String groupName, Long lecturerId, String jiraUrl,
                             String jiraProjectKey, String jiraApiToken, String jiraAdminEmail,
                             String githubRepoUrl, String githubAccessToken) {
        this.groupName = groupName;
        this.lecturerId = lecturerId;
        this.jiraUrl = jiraUrl;
        this.jiraProjectKey = jiraProjectKey;
        this.jiraApiToken = jiraApiToken;
        this.jiraAdminEmail = jiraAdminEmail;
        this.githubRepoUrl = githubRepoUrl;
        this.githubAccessToken = githubAccessToken;
    }

    public String getGroupName() { return this.groupName; }
    public Long getLecturerId() { return this.lecturerId; }
    public String getJiraUrl() { return this.jiraUrl; }
    public String getJiraProjectKey() { return this.jiraProjectKey; }
    public String getJiraApiToken() { return this.jiraApiToken; }
    public String getJiraAdminEmail() { return this.jiraAdminEmail; }
    public String getGithubRepoUrl() { return this.githubRepoUrl; }
    public String getGithubAccessToken() { return this.githubAccessToken; }

    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setLecturerId(Long lecturerId) { this.lecturerId = lecturerId; }
    public void setJiraUrl(String jiraUrl) { this.jiraUrl = jiraUrl; }
    public void setJiraProjectKey(String jiraProjectKey) { this.jiraProjectKey = jiraProjectKey; }
    public void setJiraApiToken(String jiraApiToken) { this.jiraApiToken = jiraApiToken; }
    public void setJiraAdminEmail(String jiraAdminEmail) { this.jiraAdminEmail = jiraAdminEmail; }
    public void setGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; }
    public void setGithubAccessToken(String githubAccessToken) { this.githubAccessToken = githubAccessToken; }
}


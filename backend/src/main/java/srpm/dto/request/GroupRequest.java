package srpm.dto.request;

import java.util.List;

public class GroupRequest {
    private String groupCode;
    private String groupName;
    private Long lecturerId;
    private List<Long> studentIds;
    private String jiraUrl;
    private String jiraProjectKey;
    private String jiraApiToken;
    private String jiraAdminEmail;
    private String githubRepoUrl;
    private String githubAccessToken;

    public GroupRequest() {}

    public GroupRequest(String groupCode, String groupName, Long lecturerId, List<Long> studentIds) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.lecturerId = lecturerId;
        this.studentIds = studentIds;
    }

    public String getGroupCode() { return groupCode; }
    public String getGroupName() { return groupName; }
    public Long getLecturerId() { return lecturerId; }
    public List<Long> getStudentIds() { return studentIds; }
    public String getJiraUrl() { return jiraUrl; }
    public String getJiraProjectKey() { return jiraProjectKey; }
    public String getJiraApiToken() { return jiraApiToken; }
    public String getJiraAdminEmail() { return jiraAdminEmail; }
    public String getGithubRepoUrl() { return githubRepoUrl; }
    public String getGithubAccessToken() { return githubAccessToken; }

    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setLecturerId(Long lecturerId) { this.lecturerId = lecturerId; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }
    public void setJiraUrl(String jiraUrl) { this.jiraUrl = jiraUrl; }
    public void setJiraProjectKey(String jiraProjectKey) { this.jiraProjectKey = jiraProjectKey; }
    public void setJiraApiToken(String jiraApiToken) { this.jiraApiToken = jiraApiToken; }
    public void setJiraAdminEmail(String jiraAdminEmail) { this.jiraAdminEmail = jiraAdminEmail; }
    public void setGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; }
    public void setGithubAccessToken(String githubAccessToken) { this.githubAccessToken = githubAccessToken; }
}


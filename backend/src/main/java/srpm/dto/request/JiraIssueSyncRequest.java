package srpm.dto.request;

public class JiraIssueSyncRequest {
    private Long groupId;
    private String projectKey;

    public JiraIssueSyncRequest() {}

    public JiraIssueSyncRequest(Long groupId, String projectKey) {
        this.groupId = groupId;
        this.projectKey = projectKey;
    }

    public Long getGroupId() { return groupId; }
    public String getProjectKey() { return projectKey; }

    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public void setProjectKey(String projectKey) { this.projectKey = projectKey; }
}


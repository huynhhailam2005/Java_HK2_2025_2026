package srpm.dto.request;

public class JiraSyncRequest {

    private String jiraGroupName;

    public JiraSyncRequest() {}

    public JiraSyncRequest(String jiraGroupName) {
        this.jiraGroupName = jiraGroupName;
    }

    public String getJiraGroupName() {
        return jiraGroupName;
    }

    public void setJiraGroupName(String jiraGroupName) {
        this.jiraGroupName = jiraGroupName;
    }
}


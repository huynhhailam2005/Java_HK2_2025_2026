package srpm.dto;

import java.util.List;

public class JiraIssueDto {
    private String key;
    private String issueType;
    private String summary;
    private String description;
    private String parentKey;
    private List<String> childKeys;

    public JiraIssueDto() {}

    public JiraIssueDto(String key, String issueType, String summary, String description, String parentKey) {
        this.key = key;
        this.issueType = issueType;
        this.summary = summary;
        this.description = description;
        this.parentKey = parentKey;
    }

    public String getKey() { return key; }
    public String getIssueType() { return issueType; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getParentKey() { return parentKey; }
    public List<String> getChildKeys() { return childKeys; }

    public void setKey(String key) { this.key = key; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setDescription(String description) { this.description = description; }
    public void setParentKey(String parentKey) { this.parentKey = parentKey; }
    public void setChildKeys(List<String> childKeys) { this.childKeys = childKeys; }
}


package srpm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubMemberMappingDto {
    private Long groupMemberId;
    private Long studentId;
    private String studentCode;
    private String studentUsername;
    private String githubUsername;
    private Long groupId;
    private String groupCode;
    private String groupName;
    private String githubRepoUrl;

    @JsonProperty("isMapped")
    private boolean isMapped;
    private String mappingStatus;

    public GitHubMemberMappingDto() {}

    public GitHubMemberMappingDto(Long groupMemberId, Long studentId, String studentCode,
                                   String studentUsername, String githubUsername,
                                   Long groupId, String groupCode, String groupName,
                                   String githubRepoUrl, boolean isMapped, String mappingStatus) {
        this.groupMemberId = groupMemberId;
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentUsername = studentUsername;
        this.githubUsername = githubUsername;
        this.groupId = groupId;
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.githubRepoUrl = githubRepoUrl;
        this.isMapped = isMapped;
        this.mappingStatus = mappingStatus;
    }

    public Long getGroupMemberId() { return groupMemberId; }
    public void setGroupMemberId(Long groupMemberId) { this.groupMemberId = groupMemberId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGithubRepoUrl() { return githubRepoUrl; }
    public void setGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; }

    public boolean isMapped() { return isMapped; }
    public void setMapped(boolean mapped) { isMapped = mapped; }

    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }
}


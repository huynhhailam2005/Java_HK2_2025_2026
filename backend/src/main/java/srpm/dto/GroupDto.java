package srpm.dto;

import srpm.model.Group;
import srpm.model.GroupMember;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupDto {
    private Long id;
    private String groupId;
    private String groupName;
    private Long lecturerId;
    private String lecturerUsername;
    private LocalDateTime createdAt;
    private List<MemberInfoDto> members;
    private List<Long> studentIds;
    private String jiraUrl;
    private String jiraProjectKey;
    private String jiraApiToken;
    private String jiraAdminEmail;
    private String githubRepoUrl;
    private String githubAccessToken;

    public static GroupDto fromEntity(Group group) {
        GroupDto dto = new GroupDto();
        dto.id = group.getId();
        dto.groupId = group.getGroupCode();
        dto.groupName = group.getGroupName();
        dto.createdAt = group.getCreatedAt();
        dto.jiraUrl = group.getJiraUrl();
        dto.jiraProjectKey = group.getJiraProjectKey();
        dto.jiraApiToken = group.getJiraApiToken();
        dto.jiraAdminEmail = group.getJiraAdminEmail();
        dto.githubRepoUrl = group.getGithubRepoUrl();
        dto.githubAccessToken = group.getGithubAccessToken();

        if (group.getLecturer() != null) {
            dto.lecturerId = group.getLecturer().getID();
            dto.lecturerUsername = group.getLecturer().getUsername();
        }

        List<MemberInfoDto> memberList = new ArrayList<>();
        List<Long> idList = new ArrayList<>();

        if (group.getGroupMembers() != null) {
            for (GroupMember gm : group.getGroupMembers()) {
                if (gm.getStudent() != null) {
                    idList.add(gm.getStudent().getId());
                    memberList.add(new MemberInfoDto(
                            gm.getStudent().getId(),
                            gm.getId(),
                            gm.getStudent().getUsername(),
                            gm.getStudent().getStudentCode(),
                            gm.getStudent().getEmail(),
                            gm.getGroupMemberRole().name(),
                            gm.getJoinedAt()
                    ));
                }
            }
        }
        dto.members = memberList;
        dto.studentIds = idList;
        return dto;
    }

    public Long getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public Long getLecturerId() { return lecturerId; }
    public String getLecturerUsername() { return lecturerUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<MemberInfoDto> getMembers() { return members; }
    public void setMembers(List<MemberInfoDto> members) { this.members = members; }
    public List<Long> getStudentIds() { return studentIds; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }
    public String getJiraUrl() { return jiraUrl; }
    public String getJiraProjectKey() { return jiraProjectKey; }
    public String getJiraApiToken() { return jiraApiToken; }
    public String getJiraAdminEmail() { return jiraAdminEmail; }
    public String getGithubRepoUrl() { return githubRepoUrl; }
    public String getGithubAccessToken() { return githubAccessToken; }
}
package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(name = "group_code", length = 20, nullable = false, unique = true)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;


    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private Set<GroupMember> groupMembers = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "jira_url", length = 500)
    private String jiraUrl;

    @Column(name = "jira_project_key", length = 50)
    private String jiraProjectKey;

    @Column(name = "jira_api_token")
    private String jiraApiToken;

    @Column(name = "jira_admin_email", length = 255)
    private String jiraAdminEmail;

    @Column(name = "github_repo_url", length = 500)
    private String githubRepoUrl;

    @Column(name = "github_access_token")
    private String githubAccessToken;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Group() {}

    public Group(String groupCode, String groupName, Lecturer lecturer) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.lecturer = lecturer;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getGroupCode() { return groupCode; }
    public String getGroupName() { return groupName; }
    public Lecturer getLecturer() { return lecturer; }
    public Set<GroupMember> getGroupMembers() { return groupMembers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getJiraUrl() { return jiraUrl; }
    public String getJiraProjectKey() { return jiraProjectKey; }
    public String getJiraApiToken() { return jiraApiToken; }
    public String getJiraAdminEmail() { return jiraAdminEmail; }
    public String getGithubRepoUrl() { return githubRepoUrl; }
    public String getGithubAccessToken() { return githubAccessToken; }

    public void setId(Long id) { this.id = id; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }
    public void setGroupMembers(Set<GroupMember> groupMembers) { this.groupMembers = groupMembers; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setJiraUrl(String jiraUrl) { this.jiraUrl = jiraUrl; }
    public void setJiraProjectKey(String jiraProjectKey) { this.jiraProjectKey = jiraProjectKey; }
    public void setJiraApiToken(String jiraApiToken) { this.jiraApiToken = jiraApiToken; }
    public void setJiraAdminEmail(String jiraAdminEmail) { this.jiraAdminEmail = jiraAdminEmail; }
    public void setGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; }
    public void setGithubAccessToken(String githubAccessToken) { this.githubAccessToken = githubAccessToken; }
}


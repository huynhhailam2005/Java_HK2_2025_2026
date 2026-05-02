package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import srpm.model.SyncStatus;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Long id;

    @Column(name = "issue_code", length = 20, unique = true, nullable = true)
    private String issueCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_member_id")
    private GroupMember assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private Issue parent;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IssueStatus status = IssueStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 50)
    private IssueType issueType = IssueType.TASK;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = IssueStatus.TODO;
        }
        if (this.issueType == null) {
            this.issueType = IssueType.TASK;
        }
    }

    public Issue() {}

    public Issue(String issueCode, Group group, String title, String description, LocalDateTime deadline) {
        this.issueCode = issueCode;
        this.group = group;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public Issue(String issueCode, Group group, String title, String description, LocalDateTime deadline, IssueType issueType) {
        this.issueCode = issueCode;
        this.group = group;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.issueType = issueType;
    }

    public Long getId() { return id; }
    public String getIssueCode() { return issueCode; }
    public Group getGroup() { return group; }
    public GroupMember getAssignedTo() { return assignedTo; }
    public Issue getParent() { return parent; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getDeadline() { return deadline; }
    public IssueStatus getStatus() { return status; }
    public IssueType getIssueType() { return issueType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setId(Long id) { this.id = id; }
    public void setIssueCode(String issueCode) { this.issueCode = issueCode; }
    public void setGroup(Group group) { this.group = group; }
    public void setAssignedTo(GroupMember assignedTo) { this.assignedTo = assignedTo; }
    public void setParent(Issue parent) { this.parent = parent; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public void setIssueType(IssueType issueType) { this.issueType = issueType; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }

}


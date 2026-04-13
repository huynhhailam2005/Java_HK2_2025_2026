package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @Column(name = "submission_code", length = 20, nullable = false, unique = true)
    private String submissionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_member_id", nullable = false)
    private GroupMember submittedBy;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    public Submission() {}

    public Submission(String submissionCode, Issue issue, GroupMember submittedBy, String content) {
        this.submissionCode = submissionCode;
        this.issue = issue;
        this.submittedBy = submittedBy;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getSubmissionCode() { return submissionCode; }
    public Issue getIssue() { return issue; }
    public GroupMember getSubmittedBy() { return submittedBy; }
    public String getContent() { return content; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public void setId(Long id) { this.id = id; }
    public void setSubmissionCode(String submissionCode) { this.submissionCode = submissionCode; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public void setSubmittedBy(GroupMember submittedBy) { this.submittedBy = submittedBy; }
    public void setContent(String content) { this.content = content; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
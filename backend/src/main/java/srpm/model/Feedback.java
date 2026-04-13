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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @Column(name = "feedback_code", length = 20, nullable = false, unique = true)
    private String feedbackCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_by_user_id", nullable = false)
    private User feedbackBy;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Min(1)
    @Max(5)
    @Column(name = "rating")
    private Integer rating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Feedback() {}

    public Feedback(String feedbackCode, Submission submission, User feedbackBy, String content, Integer rating) {
        this.feedbackCode = feedbackCode;
        this.submission = submission;
        this.feedbackBy = feedbackBy;
        this.content = content;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public String getFeedbackCode() { return feedbackCode; }
    public Submission getSubmission() { return submission; }
    public User getFeedbackBy() { return feedbackBy; }
    public String getContent() { return content; }
    public Integer getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setFeedbackCode(String feedbackCode) { this.feedbackCode = feedbackCode; }
    public void setSubmission(Submission submission) { this.submission = submission; }
    public void setFeedbackBy(User feedbackBy) { this.feedbackBy = feedbackBy; }
    public void setContent(String content) { this.content = content; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
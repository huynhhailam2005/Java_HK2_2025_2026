package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "reviewer_id", nullable = false, length = 50)
    private String reviewerId;

    @Column(name = "group_id", nullable = false, length = 50)
    private String groupId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructor mặc định (Bắt buộc phải có cho Spring Boot)
    public Feedback() {}

    // Constructor đầy đủ tham số
    public Feedback(String id, String content, int rating, String reviewerId, String groupId, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.rating = rating;
        this.reviewerId = reviewerId;
        this.groupId = groupId;
        this.createdAt = createdAt;
    }

    // --- GETTER & SETTER ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
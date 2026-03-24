package srpm.model;

import java.time.LocalDateTime;

public class Feedback {
    private String id;
    private String content;
    private int rating;
    private String reviewerId;
    private String groupId;
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
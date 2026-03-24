package srpm.model;

import java.time.LocalDateTime;

public class Topic {
    private String id;
    private String title;
    private String description;
    private TopicStatus status;
    private String lecturerId;
    private String studentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Topic() {}

    public Topic(String id, String title, String description, TopicStatus status,
                 String lecturerId, String studentId,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.lecturerId = lecturerId;
        this.studentId = studentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TopicStatus getStatus() { return status; }
    public String getLecturerId() { return lecturerId; }
    public String getStudentId() { return studentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TopicStatus status) { this.status = status; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

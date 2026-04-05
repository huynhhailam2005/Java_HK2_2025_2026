package srpm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments")
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"group", "assignee", "createdBy"})
    private Task task;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"password", "email"})
    private User author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public TaskComment() {}

    public TaskComment(String content, Task task, User author) {
        this.content = content;
        this.task = task;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public Task getTask() { return task; }
    public User getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setTask(Task task) { this.task = task; }
    public void setAuthor(User author) { this.author = author; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

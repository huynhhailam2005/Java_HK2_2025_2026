package srpm.dto.response;

import srpm.model.TaskComment;

import java.time.LocalDateTime;

public class TaskCommentDto {
    private Long id;
    private String content;
    private Long taskId;
    private String authorId;
    private String authorUsername;
    private LocalDateTime createdAt;

    public TaskCommentDto() {}

    public static TaskCommentDto fromEntity(TaskComment comment) {
        TaskCommentDto dto = new TaskCommentDto();
        dto.id = comment.getId();
        dto.content = comment.getContent();
        dto.createdAt = comment.getCreatedAt();
        if (comment.getTask() != null) {
            dto.taskId = comment.getTask().getId();
        }
        if (comment.getAuthor() != null) {
            dto.authorId = comment.getAuthor().getID();
            dto.authorUsername = comment.getAuthor().getUsername();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public Long getTaskId() { return taskId; }
    public String getAuthorId() { return authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

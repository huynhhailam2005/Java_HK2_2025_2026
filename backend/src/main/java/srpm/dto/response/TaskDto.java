package srpm.dto.response;

import srpm.model.Task;
import srpm.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private String groupId;
    private String groupTitle;
    private String assigneeId;
    private String assigneeUsername;
    private String createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;

    public TaskDto() {}

    public static TaskDto fromEntity(Task task) {
        TaskDto dto = new TaskDto();
        dto.id = task.getId();
        dto.title = task.getTitle();
        dto.description = task.getDescription();
        dto.dueDate = task.getDueDate();
        dto.status = task.getStatus();
        dto.createdAt = task.getCreatedAt();

        if (task.getGroup() != null) {
            dto.groupId = task.getGroup().getId();
            dto.groupTitle = task.getGroup().getTitle();
        }
        if (task.getAssignee() != null) {
            dto.assigneeId = task.getAssignee().getID();
            dto.assigneeUsername = task.getAssignee().getUsername();
        }
        if (task.getCreatedBy() != null) {
            dto.createdById = task.getCreatedBy().getID();
            dto.createdByUsername = task.getCreatedBy().getUsername();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public TaskStatus getStatus() { return status; }
    public String getGroupId() { return groupId; }
    public String getGroupTitle() { return groupTitle; }
    public String getAssigneeId() { return assigneeId; }
    public String getAssigneeUsername() { return assigneeUsername; }
    public String getCreatedById() { return createdById; }
    public String getCreatedByUsername() { return createdByUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setGroupTitle(String groupTitle) { this.groupTitle = groupTitle; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    public void setAssigneeUsername(String assigneeUsername) { this.assigneeUsername = assigneeUsername; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

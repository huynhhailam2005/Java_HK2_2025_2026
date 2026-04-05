package srpm.dto.request;

import java.time.LocalDate;

public class TaskRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private String assigneeId;   // student id, có thể null
    private String createdById;  // user id của người tạo

    public TaskRequest() {}

    public TaskRequest(String title, String description, LocalDate dueDate,
                       String assigneeId, String createdById) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.assigneeId = assigneeId;
        this.createdById = createdById;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getAssigneeId() { return assigneeId; }
    public String getCreatedById() { return createdById; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }
}

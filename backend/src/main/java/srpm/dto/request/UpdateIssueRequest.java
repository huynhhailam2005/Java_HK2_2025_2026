package srpm.dto.request;

import srpm.model.IssueStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdateIssueRequest {

    private String title;
    private String description;
    private String deadline;
    private Long parentId;
    private Long assignedToMemberId;
    private boolean clearAssignee = false;
    private String status;

    public UpdateIssueRequest() {}

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
    public Long getParentId() { return parentId; }
    public Long getAssignedToMemberId() { return assignedToMemberId; }
    public boolean isClearAssignee() { return clearAssignee; }
    public String getStatus() { return status; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setAssignedToMemberId(Long assignedToMemberId) { this.assignedToMemberId = assignedToMemberId; }
    public void setClearAssignee(boolean clearAssignee) { this.clearAssignee = clearAssignee; }
    public void setStatus(String status) { this.status = status; }

    /** Chuyển deadline từ String sang LocalDateTime (hỗ trợ cả 2 format) */
    public LocalDateTime getDeadlineAsLocalDateTime() {
        if (deadline == null || deadline.isBlank()) return null;
        try {
            String trimmed = deadline.trim();
            if (trimmed.contains("T")) {
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } else {
                return LocalDateTime.parse(trimmed + "T23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể parse deadline: " + deadline, e);
        }
    }
}

package srpm.dto.request;

import srpm.model.IssueType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateIssueRequest {

    private Long groupId;
    private String title;
    private String description;
    private String deadline;
    private IssueType issueType;
    private Long parentId;
    private Long assignedToMemberId;
    public CreateIssueRequest() {}

    public CreateIssueRequest(Long groupId, String title, String description,
                             String deadline, IssueType issueType,
                             Long parentId, Long assignedToMemberId) {
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.issueType = issueType;
        this.parentId = parentId;
        this.assignedToMemberId = assignedToMemberId;
    }

    public Long getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
    public IssueType getIssueType() { return issueType; }
    public Long getParentId() { return parentId; }
    public Long getAssignedToMemberId() { return assignedToMemberId; }

    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setIssueType(IssueType issueType) { this.issueType = issueType; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setAssignedToMemberId(Long assignedToMemberId) { this.assignedToMemberId = assignedToMemberId; }

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


package srpm.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import srpm.model.IssueType;
import java.time.LocalDateTime;

public class CreateIssueRequest {

    private Long groupId;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;

    private IssueType issueType;

    // parentId: dùng cho Standard Issue (TASK, STORY, BUG) để chỉ parent epic,
    // hoặc cho SubTask để chỉ parent task/story/epic
    private Long parentId;

    // assignedToMemberId: ID của GroupMember để assign issue
    private Long assignedToMemberId;

    public CreateIssueRequest() {}

    public CreateIssueRequest(Long groupId, String title, String description,
                             LocalDateTime deadline, IssueType issueType,
                             Long parentId, Long assignedToMemberId) {
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.issueType = issueType;
        this.parentId = parentId;
        this.assignedToMemberId = assignedToMemberId;
    }

    // Getters
    public Long getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getDeadline() { return deadline; }
    public IssueType getIssueType() { return issueType; }
    public Long getParentId() { return parentId; }
    public Long getAssignedToMemberId() { return assignedToMemberId; }

    // Setters
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setIssueType(IssueType issueType) { this.issueType = issueType; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setAssignedToMemberId(Long assignedToMemberId) { this.assignedToMemberId = assignedToMemberId; }
}


package srpm.dto;

import srpm.model.Issue;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public class IssueDetailDto {
    public Long issueId;
    public String issueCode;
    public String title;
    public String description;
    public String type;
    public String status;
    public String assignedTo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDateTime deadline;
    public Long groupId;
    public String groupName;
    public Long parentId;
    public String parentCode;
    public String parentTitle;
    public boolean submitted;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public LocalDateTime updatedAt;
    public IssueDetailDto() {}

    public IssueDetailDto(Issue issue) {
        if (issue == null) return;

        this.issueId = issue.getId();
        this.issueCode = issue.getIssueCode();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.type = (issue.getIssueType() != null) ? issue.getIssueType().name() : "TASK";
        this.status = (issue.getStatus() != null) ? issue.getStatus().name() : "TODO";
        if (issue.getAssignedTo() != null && issue.getAssignedTo().getStudent() != null) {
            this.assignedTo = issue.getAssignedTo().getStudent().getUsername();
        } else {
            this.assignedTo = "Chưa gán";
        }

        this.deadline = issue.getDeadline();

        if (issue.getGroup() != null) {
            this.groupId = issue.getGroup().getId();
            this.groupName = issue.getGroup().getGroupName();
        }

        if (issue.getParent() != null) {
            this.parentId = issue.getParent().getId();
            this.parentCode = issue.getParent().getIssueCode();
            this.parentTitle = issue.getParent().getTitle();
        }

        this.createdAt = issue.getCreatedAt();
        this.updatedAt = issue.getCreatedAt();
    }

    public static IssueDetailDto fromEntity(Issue issue, boolean submitted) {
        IssueDetailDto dto = new IssueDetailDto(issue);
        dto.submitted = submitted;
        return dto;
    }

    public static IssueDetailDto fromEntity(Issue issue) {
        return fromEntity(issue, false);
    }

    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }

    public String getIssueCode() { return issueCode; }
    public void setIssueCode(String issueCode) { this.issueCode = issueCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }

    public String getParentTitle() { return parentTitle; }
    public void setParentTitle(String parentTitle) { this.parentTitle = parentTitle; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

}
package srpm.dto.response;

import srpm.model.Issue;

import java.time.LocalDateTime;

public class IssueDetailDto {
    public Long issueId;
    public String issueCode;
    public String title;
    public String description;
    public String type;
    public String status;
    public String assignedTo;
    public LocalDateTime deadline;

    public IssueDetailDto() {
    }

    public IssueDetailDto(Issue issue) {
        this.issueId = issue.getId();
        this.issueCode = issue.getIssueCode();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.type = issue.getIssueType() != null ? issue.getIssueType().toString() : "";
        this.status = issue.getStatus() != null ? issue.getStatus().toString() : "";
        this.assignedTo = issue.getAssignedTo() != null ?
                issue.getAssignedTo().getStudent().getUsername() : "Chưa gán";
        this.deadline = issue.getDeadline();
    }

    public static IssueDetailDto fromEntity(Issue issue) {
        return new IssueDetailDto(issue);
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getIssueCode() {
        return issueCode;
    }

    public void setIssueCode(String issueCode) {
        this.issueCode = issueCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}


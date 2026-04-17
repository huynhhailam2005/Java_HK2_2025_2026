package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.model.Group;
import srpm.model.Issue;
import srpm.model.IssueStatus;
import srpm.repository.GroupRepository;
import srpm.repository.IssueRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "http://localhost:5173")
public class ProgressReportController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private IssueRepository issueRepository;

    /**
     * Lấy báo cáo tiến độ dự án của nhóm
     * GET /api/progress/groups/{groupId}
     * Chỉ cho phép Lecturer, Admin, và các thành viên của nhóm
     */
    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> getGroupProgressReport(@PathVariable Long groupId) {
        try {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

            // Lấy tất cả issues của nhóm (không xóa)
            List<Issue> allIssues = issueRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

            // Lọc chỉ lấy issues chính (parentId = null) và không bị xóa
            List<Issue> mainIssues = allIssues.stream()
                    .filter(issue -> issue.getParent() == null && !issue.getIsDeleted())
                    .collect(Collectors.toList());

            // Tính thống kê
            int total = mainIssues.size();
            int completed = (int) mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                    .count();
            int inProgress = (int) mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS)
                    .count();
            int todo = (int) mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.TODO)
                    .count();
            int cancelled = (int) mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.CANCELLED)
                    .count();

            double completedPercentage = total == 0 ? 0 : (double) completed * 100 / total;

            Map<String, Object> report = new LinkedHashMap<>();
            report.put("groupId", group.getId());
            report.put("groupName", group.getGroupName());
            report.put("groupCode", group.getGroupCode());
            report.put("totalIssues", total);
            report.put("completedIssues", completed);
            report.put("inProgressIssues", inProgress);
            report.put("todoIssues", todo);
            report.put("cancelledIssues", cancelled);
            report.put("completedPercentage", String.format("%.2f", completedPercentage) + "%");
            report.put("progress", completedPercentage);

            // Thêm chi tiết theo trạng thái
            Map<String, Object> issuesByStatus = new LinkedHashMap<>();
            issuesByStatus.put("DONE", mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                    .map(issue -> new IssueDetail(issue))
                    .collect(Collectors.toList()));
            issuesByStatus.put("IN_PROGRESS", mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS)
                    .map(issue -> new IssueDetail(issue))
                    .collect(Collectors.toList()));
            issuesByStatus.put("TODO", mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.TODO)
                    .map(issue -> new IssueDetail(issue))
                    .collect(Collectors.toList()));
            issuesByStatus.put("CANCELLED", mainIssues.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.CANCELLED)
                    .map(issue -> new IssueDetail(issue))
                    .collect(Collectors.toList()));

            report.put("issuesByStatus", issuesByStatus);

            return ResponseEntity.ok(new ApiResponse(true, "Lấy báo cáo tiến độ thành công", report));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Nested class để convert Issue thành DTO
     */
    private static class IssueDetail {
        public Long issueId;
        public String issueCode;
        public String title;
        public String description;
        public String type;
        public String status;
        public String assignedTo;
        public LocalDateTime deadline;

        public IssueDetail(Issue issue) {
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
    }
}


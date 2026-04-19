package srpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.response.IssueDetailDto;
import srpm.model.Group;
import srpm.model.Issue;
import srpm.model.IssueStatus;
import srpm.repository.GroupRepository;
import srpm.repository.IssueRepository;
import srpm.exception.ResourceNotFoundException;
import srpm.service.IProgressReportService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProgressReportService implements IProgressReportService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressReportService.class);

    private final GroupRepository groupRepository;
    private final IssueRepository issueRepository;

    @Autowired
    public ProgressReportService(GroupRepository groupRepository, IssueRepository issueRepository) {
        this.groupRepository = groupRepository;
        this.issueRepository = issueRepository;
    }

    // Sinh báo cáo tiến độ cho nhóm
    @Override
    public Map<String, Object> generateProgressReport(Long groupId) {
        logger.info("Generating progress report for group {}", groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm không tồn tại: " + groupId));

        // Lấy tất cả issues chính (không phải sub-task)
        List<Issue> allIssues = issueRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        List<Issue> mainIssues = allIssues.stream()
                .filter(issue -> issue.getParent() == null && !issue.getIsDeleted())
                .collect(Collectors.toList());

        // Tính thống kê
        ProgressStats stats = calculateStats(mainIssues);

        // Xây dựng báo cáo
        Map<String, Object> report = buildReport(group, stats, mainIssues);

        logger.info("Progress report generated for group {}: {} total, {} completed",
                groupId, stats.total, stats.completed);

        return report;
    }

    // Tính số lượng theo từng status
    private ProgressStats calculateStats(List<Issue> mainIssues) {
        ProgressStats stats = new ProgressStats();
        stats.total = mainIssues.size();
        stats.completed = (int) mainIssues.stream()
                .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                .count();
        stats.inProgress = (int) mainIssues.stream()
                .filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS)
                .count();
        stats.todo = (int) mainIssues.stream()
                .filter(issue -> issue.getStatus() == IssueStatus.TODO)
                .count();
        stats.cancelled = (int) mainIssues.stream()
                .filter(issue -> issue.getStatus() == IssueStatus.CANCELLED)
                .count();
        stats.completedPercentage = stats.total == 0 ? 0 : (double) stats.completed * 100 / stats.total;

        return stats;
    }

    // Xây dựng chi tiết báo cáo kèm danh sách issues
    private Map<String, Object> buildReport(Group group, ProgressStats stats, List<Issue> mainIssues) {
        Map<String, Object> report = new LinkedHashMap<>();

        // Thông tin nhóm
        report.put("groupId", group.getId());
        report.put("groupName", group.getGroupName());
        report.put("groupCode", group.getGroupCode());

        // Thống kê
        report.put("totalIssues", stats.total);
        report.put("completedIssues", stats.completed);
        report.put("inProgressIssues", stats.inProgress);
        report.put("todoIssues", stats.todo);
        report.put("cancelledIssues", stats.cancelled);
        report.put("completedPercentage", String.format("%.2f", stats.completedPercentage) + "%");
        report.put("progress", stats.completedPercentage);

        // Chi tiết theo trạng thái
        Map<String, Object> issuesByStatus = new LinkedHashMap<>();
        issuesByStatus.put("DONE", groupIssuesByStatus(mainIssues, IssueStatus.DONE));
        issuesByStatus.put("IN_PROGRESS", groupIssuesByStatus(mainIssues, IssueStatus.IN_PROGRESS));
        issuesByStatus.put("TODO", groupIssuesByStatus(mainIssues, IssueStatus.TODO));
        issuesByStatus.put("CANCELLED", groupIssuesByStatus(mainIssues, IssueStatus.CANCELLED));

        report.put("issuesByStatus", issuesByStatus);

        return report;
    }

    // Lấy issues theo status, convert thành DTO
    private List<IssueDetailDto> groupIssuesByStatus(List<Issue> issues, IssueStatus status) {
        return issues.stream()
                .filter(issue -> issue.getStatus() == status)
                .map(IssueDetailDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Lưu trữ thống kê tạm thời
    private static class ProgressStats {
        int total;
        int completed;
        int inProgress;
        int todo;
        int cancelled;
        double completedPercentage;
    }
}


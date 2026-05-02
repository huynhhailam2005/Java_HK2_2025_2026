package srpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.IssueDetailDto;
import srpm.model.*;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IGroupRepository;
import srpm.repository.IIssueRepository;
import srpm.exception.ResourceNotFoundException;
import srpm.service.IProgressReportService;
import srpm.service.IGitHubService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProgressReportService implements IProgressReportService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressReportService.class);

    private final IGroupRepository IGroupRepository;
    private final IIssueRepository IIssueRepository;
    private final IGroupMemberRepository IGroupMemberRepository;
    private final IGitHubService gitHubService;

    @Autowired
    public ProgressReportService(IGroupRepository IGroupRepository,
                                 IIssueRepository IIssueRepository,
                                 IGroupMemberRepository IGroupMemberRepository,
                                 IGitHubService gitHubService) {
        this.IGroupRepository = IGroupRepository;
        this.IIssueRepository = IIssueRepository;
        this.IGroupMemberRepository = IGroupMemberRepository;
        this.gitHubService = gitHubService;
    }

    // Sinh báo cáo tiến độ cho nhóm
    @Override
    public Map<String, Object> generateProgressReport(Long groupId) {
        logger.info("Generating progress report for group {}", groupId);

        Group group = IGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm không tồn tại: " + groupId));

        List<Issue> allIssues = IIssueRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        List<Issue> mainIssues = allIssues.stream()
                .filter(issue -> issue.getParent() == null)
                .collect(Collectors.toList());

        List<GroupMember> members = IGroupMemberRepository.findByGroup(groupId);

        ProgressStats stats = calculateStats(mainIssues);

        List<Map<String, Object>> memberContributions = calculateMemberContributions(allIssues, members);

        // Xây dựng báo cáo
        Map<String, Object> report = buildReport(group, stats, mainIssues, memberContributions, members.size());

        logger.info("Progress report generated for group {}: {} total, {} completed, {} members",
                groupId, stats.total, stats.completed, members.size());

        return report;
    }

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
        stats.completedPercentage = stats.total == 0 ? 0 : (double) stats.completed * 100 / stats.total;

        return stats;
    }

    private List<Map<String, Object>> calculateMemberContributions(List<Issue> allIssues, List<GroupMember> members) {
        List<Map<String, Object>> contributions = new ArrayList<>();

        Long groupId = null;
        if (!allIssues.isEmpty()) {
            groupId = allIssues.get(0).getGroup().getId();
        }

        for (GroupMember gm : members) {
            Student student = gm.getStudent();
            if (student == null) continue;

            long assignedIssues = allIssues.stream()
                    .filter(i -> i.getAssignedTo() != null
                            && i.getAssignedTo().getId().equals(gm.getId()))
                    .count();
            long completedByMember = allIssues.stream()
                    .filter(i -> i.getAssignedTo() != null
                            && i.getAssignedTo().getId().equals(gm.getId())
                            && i.getStatus() == IssueStatus.DONE)
                    .count();

            int commitCount = 0;
            try {
                if (student.getGithubUsername() != null && !student.getGithubUsername().isEmpty() && groupId != null) {
                    Group group = IGroupRepository.findById(groupId).orElse(null);

                    if (group != null && group.getGithubRepoUrl() != null && !group.getGithubRepoUrl().isEmpty()) {
                        Map<String, Object> commitStats = gitHubService.getCommitStats(
                                extractRepoOwner(group.getGithubRepoUrl()),
                                extractRepoName(group.getGithubRepoUrl()),
                                student.getGithubUsername(),
                                group.getGithubAccessToken()
                        );

                        if (commitStats.containsKey("totalCommits")) {
                            Object totalCommits = commitStats.get("totalCommits");
                            if (totalCommits instanceof Integer) {
                                commitCount = (Integer) totalCommits;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to fetch commit count for {}: {}", student.getGithubUsername(), e.getMessage());
            }

            Map<String, Object> mc = new LinkedHashMap<>();
            mc.put("memberId", gm.getId());
            mc.put("studentCode", student.getStudentCode() != null ? student.getStudentCode() : "");
            mc.put("username", student.getUsername());
            mc.put("githubUsername", student.getGithubUsername() != null ? student.getGithubUsername() : "—");
            mc.put("role", gm.getGroupMemberRole() == GroupMemberRole.TEAM_LEADER ? "Nhóm trưởng" : "Thành viên");
            mc.put("assignedIssues", assignedIssues);
            mc.put("completedIssues", completedByMember);
            mc.put("commitCount", commitCount);
            mc.put("completionRate", assignedIssues == 0 ? 0 : Math.round((double) completedByMember * 100 / assignedIssues));

            contributions.add(mc);
        }

        return contributions;
    }

    private String extractRepoOwner(String githubUrl) {
        String[] parts = githubUrl.replace(".git", "").split("/");
        return parts[parts.length - 2];
    }

    private String extractRepoName(String githubUrl) {
        String[] parts = githubUrl.replace(".git", "").split("/");
        return parts[parts.length - 1];
    }

    private Map<String, Object> buildReport(Group group, ProgressStats stats, List<Issue> mainIssues,
                                            List<Map<String, Object>> memberContributions, int totalMembers) {
        Map<String, Object> report = new LinkedHashMap<>();

        report.put("groupId", group.getId());
        report.put("groupName", group.getGroupName());
        report.put("groupCode", group.getGroupCode());
        report.put("githubRepoUrl", group.getGithubRepoUrl());

        report.put("totalIssues", stats.total);
        report.put("completedIssues", stats.completed);
        report.put("inProgressIssues", stats.inProgress);
        report.put("todoIssues", stats.todo);
        report.put("totalMembers", totalMembers);

        report.put("completedPercentage", String.format("%.2f", stats.completedPercentage) + "%");
        report.put("progress", stats.completedPercentage);

        Map<String, Object> issuesByStatus = new LinkedHashMap<>();
        issuesByStatus.put("DONE", groupIssuesByStatus(mainIssues, IssueStatus.DONE));
        issuesByStatus.put("IN_PROGRESS", groupIssuesByStatus(mainIssues, IssueStatus.IN_PROGRESS));
        issuesByStatus.put("TODO", groupIssuesByStatus(mainIssues, IssueStatus.TODO));
        report.put("issuesByStatus", issuesByStatus);

        report.put("memberContributions", memberContributions);

        report.put("commitHistory", fetchCommitHistory(group));

        return report;
    }

    private List<IssueDetailDto> groupIssuesByStatus(List<Issue> issues, IssueStatus status) {
        return issues.stream()
                .filter(issue -> issue.getStatus() == status)
                .map(IssueDetailDto::fromEntity)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchCommitHistory(Group group) {
        List<Map<String, Object>> commitHistory = new ArrayList<>();

        if (group.getGithubRepoUrl() == null || group.getGithubRepoUrl().isEmpty()) {
            return commitHistory;
        }

        try {
            Map<String, Object> teamHistory = gitHubService.getTeamCommitHistory(group.getId(), 90);
            if (teamHistory.containsKey("dailyStats")) {
                List<Map<String, Object>> dailyStats = (List<Map<String, Object>>) teamHistory.get("dailyStats");
                for (Map<String, Object> day : dailyStats) {
                    List<Map<String, Object>> dayCommits = (List<Map<String, Object>>) day.get("commits");
                    if (dayCommits != null) {
                        commitHistory.addAll(dayCommits);
                    }
                }
                commitHistory.sort((a, b) -> {
                    String dateA = (String) a.getOrDefault("date", "");
                    String dateB = (String) b.getOrDefault("date", "");
                    return dateB.compareTo(dateA);
                });
            }
        } catch (Exception e) {
            logger.warn("Could not fetch commit history: {}", e.getMessage());
        }

        return commitHistory;
    }

    private static class ProgressStats {
        int total;
        int completed;
        int inProgress;
        int todo;
        double completedPercentage;
    }
}


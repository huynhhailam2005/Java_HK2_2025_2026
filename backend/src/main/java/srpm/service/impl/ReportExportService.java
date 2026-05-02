package srpm.service.impl;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import srpm.dto.IssueDetailDto;
import srpm.model.*;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IGroupRepository;
import srpm.repository.IIssueRepository;
import srpm.service.IGitHubService;
import srpm.service.IReportExportService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportExportService implements IReportExportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportExportService.class);

    private static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(37, 99, 235);      // Blue-600
    private static final DeviceRgb COLOR_EMERALD = new DeviceRgb(16, 185, 129);     // Emerald-500
    private static final DeviceRgb COLOR_AMBER = new DeviceRgb(245, 158, 11);       // Amber-500
    private static final DeviceRgb COLOR_SLATE = new DeviceRgb(100, 116, 139);      // Slate-500
    private static final DeviceRgb COLOR_WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_LIGHT_BG = new DeviceRgb(241, 245, 249);   // Slate-100


    @Autowired
    private IGroupRepository IGroupRepository;

    @Autowired
    private IIssueRepository IIssueRepository;

    @Autowired
    private IGroupMemberRepository IGroupMemberRepository;

    @Autowired
    private IGitHubService gitHubService;

    @Override
    public byte[] exportGroupProgressPdf(Long groupId) {
        logger.info("Starting PDF export for group: {}", groupId);

        Group group = IGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại: " + groupId));

        List<Issue> allIssues = IIssueRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        List<GroupMember> members = IGroupMemberRepository.findByGroup(groupId);

        Map<String, Object> stats = calculateStatistics(allIssues, members);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(40, 40, 40, 40);

            String fontPath = "fonts/Roboto-Regular.ttf";

            PdfFont vietnameseFont = PdfFontFactory.createFont(
                    fontPath,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
            );

            document.setFont(vietnameseFont);
            addHeader(document, group);
            addProgressSection(document, stats);
            addStatsTable(document, stats);
            addMemberContributionSection(document, stats);
            addIssuesByStatusSection(document, stats);
            addCommitHistorySection(document, stats);

            document.add(new Paragraph("\n\n\n"));
            document.add(new Paragraph("Báo cáo được tạo lúc: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFontColor(COLOR_SLATE)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            logger.info("PDF exported successfully for group: {}", groupId);

        } catch (Exception e) {
            logger.error("Error generating PDF for group {}: {}", groupId, e.getMessage(), e);
            throw new RuntimeException("Lỗi sinh file PDF: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private Map<String, Object> calculateStatistics(List<Issue> allIssues, List<GroupMember> members) {
        Map<String, Object> stats = new LinkedHashMap<>();

        List<Issue> mainIssues = allIssues.stream()
                .filter(issue -> issue.getParent() == null)
                .collect(Collectors.toList());

        int total = mainIssues.size();
        int completed = (int) mainIssues.stream().filter(i -> i.getStatus() == IssueStatus.DONE).count();
        int inProgress = (int) mainIssues.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count();
        int todo = (int) mainIssues.stream().filter(i -> i.getStatus() == IssueStatus.TODO).count();
        double progressPercent = total == 0 ? 0 : (double) completed * 100 / total;

        stats.put("totalIssues", total);
        stats.put("completedIssues", completed);
        stats.put("inProgressIssues", inProgress);
        stats.put("todoIssues", todo);
        stats.put("progress", Math.round(progressPercent * 100.0) / 100.0);

        List<Map<String, Object>> memberContributions = new ArrayList<>();
        Long groupId = allIssues.isEmpty() ? null : allIssues.get(0).getGroup().getId();
        Group group = (groupId != null) ? IGroupRepository.findById(groupId).orElse(null) : null;

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
            if (student.getGithubUsername() != null && !student.getGithubUsername().isEmpty()
                    && group != null && group.getGithubRepoUrl() != null) {
                try {
                    Map<String, Object> commitStats = gitHubService.getCommitStats(
                            extractRepoOwner(group.getGithubRepoUrl()),
                            extractRepoName(group.getGithubRepoUrl()),
                            student.getGithubUsername(),
                            group.getGithubAccessToken()
                    );
                    Object totalCommits = commitStats.get("totalCommits");
                    if (totalCommits instanceof Integer) {
                        commitCount = (Integer) totalCommits;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to fetch commit count for {}: {}", student.getGithubUsername(), e.getMessage());
                }
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
            memberContributions.add(mc);
        }
        stats.put("memberContributions", memberContributions);

        Map<String, List<IssueDetailDto>> issuesByStatus = new LinkedHashMap<>();
        issuesByStatus.put("DONE", groupIssuesByStatus(mainIssues, IssueStatus.DONE));
        issuesByStatus.put("IN_PROGRESS", groupIssuesByStatus(mainIssues, IssueStatus.IN_PROGRESS));
        issuesByStatus.put("TODO", groupIssuesByStatus(mainIssues, IssueStatus.TODO));
        stats.put("issuesByStatus", issuesByStatus);

        List<Map<String, Object>> commitHistory = new ArrayList<>();
        try {
            if (group != null && group.getGithubRepoUrl() != null && !group.getGithubRepoUrl().isEmpty()) {
                Map<String, Object> teamHistory = gitHubService.getTeamCommitHistory(groupId, 90);
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
            }
        } catch (Exception e) {
            logger.warn("Could not fetch commit history for PDF: {}", e.getMessage());
        }
        stats.put("commitHistory", commitHistory);
        stats.put("totalCommits", commitHistory.size());

        stats.put("totalMembers", members.size());

        return stats;
    }

    private List<IssueDetailDto> groupIssuesByStatus(List<Issue> issues, IssueStatus status) {
        return issues.stream()
                .filter(issue -> issue.getStatus() == status)
                .map(issue -> {
                    IssueDetailDto dto = IssueDetailDto.fromEntity(issue);
                    // Check submission status
                    dto.submitted = issue.getSyncStatus() == SyncStatus.SYNCED;
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void addHeader(Document document, Group group) {
        Paragraph title = new Paragraph("BÁO CÁO TIẾN ĐỘ NHÓM")
                .setFontColor(COLOR_PRIMARY)
                .setFontSize(22)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        addInfoCell(infoTable, "Mã nhóm:", group.getGroupCode() != null ? group.getGroupCode() : "—");
        addInfoCell(infoTable, "Tên nhóm:", group.getGroupName());
        addInfoCell(infoTable, "Giảng viên:", group.getLecturer() != null ? group.getLecturer().getUsername() : "—");
        addInfoCell(infoTable, "Ngày tạo:", group.getCreatedAt() != null
                ? group.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void addInfoCell(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label).setBold().setFontSize(10))
                .setBackgroundColor(COLOR_LIGHT_BG)
                .setPadding(6);
        Cell valueCell = new Cell().add(new Paragraph(value).setFontSize(10))
                .setPadding(6);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addProgressSection(Document document, Map<String, Object> stats) {
        double progress = (double) stats.get("progress");
        Paragraph progressTitle = new Paragraph("TIẾN ĐỘ TỔNG THỂ")
                .setBold()
                .setFontSize(14)
                .setFontColor(COLOR_PRIMARY);
        document.add(progressTitle);

        Paragraph percent = new Paragraph(String.format("%.1f%%", progress))
                .setFontSize(36)
                .setBold()
                .setFontColor(COLOR_EMERALD)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(percent);

        document.add(new Paragraph("\n"));
    }

    private void addStatsTable(Document document, Map<String, Object> stats) {
        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        addStatCell(statsTable, "Tổng Issues", String.valueOf(stats.get("totalIssues")), COLOR_PRIMARY);
        addStatCell(statsTable, "Hoàn thành", String.valueOf(stats.get("completedIssues")), COLOR_EMERALD);
        addStatCell(statsTable, "Đang làm", String.valueOf(stats.get("inProgressIssues")), COLOR_AMBER);
        addStatCell(statsTable, "Chưa làm", String.valueOf(stats.get("todoIssues")), COLOR_SLATE);

        document.add(statsTable);
        document.add(new Paragraph("\n\n"));
    }

    private void addStatCell(Table table, String label, String value, DeviceRgb color) {
        Cell cell = new Cell()
                .setBackgroundColor(COLOR_LIGHT_BG)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph(label)
                .setFontSize(9)
                .setFontColor(COLOR_SLATE)
                .setBold());
        cell.add(new Paragraph(value)
                .setFontSize(24)
                .setFontColor(color)
                .setBold());
        table.addCell(cell);
    }

    @SuppressWarnings("unchecked")
    private void addMemberContributionSection(Document document, Map<String, Object> stats) {
        Paragraph sectionTitle = new Paragraph("PHÂN CÔNG & THỰC HIỆN CÔNG VIỆC")
                .setBold()
                .setFontSize(14)
                .setFontColor(COLOR_PRIMARY)
                .setMarginTop(10);
        document.add(sectionTitle);

        List<Map<String, Object>> members = (List<Map<String, Object>>) stats.get("memberContributions");

        if (members.isEmpty()) {
            document.add(new Paragraph("Chưa có thành viên nào trong nhóm.")
                    .setFontColor(COLOR_SLATE)
                    .setFontSize(10));
            return;
        }

        Table memberTable = new Table(UnitValue.createPercentArray(new float[]{10, 15, 12, 12, 12, 12, 12, 15}))
                .setWidth(UnitValue.createPercentValue(100))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"Mã SV", "Họ tên", "Vai trò", "GitHub", "Được giao", "Hoàn thành", "Commits", "Tỉ lệ"};
        for (String h : headers) {
            Cell headerCell = new Cell().add(new Paragraph(h).setBold().setFontSize(8).setFontColor(COLOR_WHITE))
                    .setBackgroundColor(COLOR_PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5);
            memberTable.addHeaderCell(headerCell);
        }

        for (Map<String, Object> m : members) {
            addMemberCell(memberTable, String.valueOf(m.get("studentCode")));
            addMemberCell(memberTable, String.valueOf(m.get("username")));
            addMemberCell(memberTable, String.valueOf(m.get("role")));
            addMemberCell(memberTable, String.valueOf(m.get("githubUsername")));
            addMemberCell(memberTable, String.valueOf(m.get("assignedIssues")));
            addMemberCell(memberTable, String.valueOf(m.get("completedIssues")));
            addMemberCell(memberTable, String.valueOf(m.get("commitCount")));
            Object rate = m.get("completionRate");
            String rateStr = rate instanceof Number ? String.format("%d%%", ((Number) rate).longValue()) : "—";
            addMemberCell(memberTable, rateStr);
        }

        document.add(memberTable);
        document.add(new Paragraph("\n"));
    }

    private void addMemberCell(Table table, String text) {
        Cell cell = new Cell().add(new Paragraph(text).setFontSize(8))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4);
        table.addCell(cell);
    }

    @SuppressWarnings("unchecked")
    private void addIssuesByStatusSection(Document document, Map<String, Object> stats) {
        Paragraph sectionTitle = new Paragraph("DANH SÁCH YÊU CẦU (ISSUES) THEO TRẠNG THÁI")
                .setBold()
                .setFontSize(14)
                .setFontColor(COLOR_PRIMARY)
                .setMarginTop(10);
        document.add(sectionTitle);

        Map<String, List<IssueDetailDto>> issuesByStatus =
                (Map<String, List<IssueDetailDto>>) stats.get("issuesByStatus");

        addIssueSubList(document, "HOÀN THÀNH (DONE)", COLOR_EMERALD,
                issuesByStatus.getOrDefault("DONE", Collections.emptyList()));

        addIssueSubList(document, "ĐANG THỰC HIỆN (IN PROGRESS)", COLOR_AMBER,
                issuesByStatus.getOrDefault("IN_PROGRESS", Collections.emptyList()));

        addIssueSubList(document, "CHỜ THỰC HIỆN (TODO)", COLOR_SLATE,
                issuesByStatus.getOrDefault("TODO", Collections.emptyList()));
    }

    private String extractRepoOwner(String githubUrl) {
        String[] parts = githubUrl.replace(".git", "").split("/");
        return parts[parts.length - 2];
    }

    private String extractRepoName(String githubUrl) {
        String[] parts = githubUrl.replace(".git", "").split("/");
        return parts[parts.length - 1];
    }

    private void addIssueSubList(Document document, String title, DeviceRgb color, List<IssueDetailDto> issues) {
        Paragraph subTitle = new Paragraph(title)
                .setBold()
                .setFontSize(11)
                .setFontColor(color)
                .setMarginTop(8);
        document.add(subTitle);

        if (issues.isEmpty()) {
            document.add(new Paragraph("  (Không có issue nào)")
                    .setFontColor(COLOR_SLATE)
                    .setFontSize(9));
            return;
        }

        Table issueTable = new Table(UnitValue.createPercentArray(new float[]{12, 38, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"Mã Issue", "Tiêu đề", "Người thực hiện", "Hạn chót"};
        for (String h : headers) {
            Cell headerCell = new Cell().add(new Paragraph(h).setBold().setFontSize(8).setFontColor(COLOR_WHITE))
                    .setBackgroundColor(color)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(4);
            issueTable.addHeaderCell(headerCell);
        }

        for (IssueDetailDto issue : issues) {
            issueTable.addCell(new Cell().add(new Paragraph(
                    issue.issueCode != null ? issue.issueCode : "Chưa đồng bộ")
                    .setFontSize(8)).setPadding(4));
            issueTable.addCell(new Cell().add(new Paragraph(
                    issue.title != null ? issue.title : "")
                    .setFontSize(8)).setPadding(4));
            issueTable.addCell(new Cell().add(new Paragraph(
                    issue.assignedTo != null ? issue.assignedTo : "Chưa gán")
                    .setFontSize(8)).setPadding(4));
            issueTable.addCell(new Cell().add(new Paragraph(
                    issue.deadline != null
                            ? issue.deadline.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "—")
                    .setFontSize(8)).setPadding(4));
        }

        document.add(issueTable);
    }

    @SuppressWarnings("unchecked")
    private void addCommitHistorySection(Document document, Map<String, Object> stats) {
        List<Map<String, Object>> commits = (List<Map<String, Object>>) stats.get("commitHistory");
        int totalCommits = (int) stats.getOrDefault("totalCommits", 0);

        Paragraph sectionTitle = new Paragraph("LỊCH SỬ GIT COMMITS (" + totalCommits + " commits)")
                .setBold()
                .setFontSize(14)
                .setFontColor(new DeviceRgb(88, 80, 236))  // Indigo-500
                .setMarginTop(10);
        document.add(sectionTitle);

        if (commits == null || commits.isEmpty()) {
            document.add(new Paragraph("  (Chưa có commit nào hoặc GitHub chưa được cấu hình)")
                    .setFontColor(COLOR_SLATE)
                    .setFontSize(9));
            return;
        }

        Table commitTable = new Table(UnitValue.createPercentArray(new float[]{18, 12, 50, 20}))
                .setWidth(UnitValue.createPercentValue(100))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"Thời gian", "Tác giả", "Nội dung", "SHA"};
        for (String h : headers) {
            Cell headerCell = new Cell().add(new Paragraph(h).setBold().setFontSize(8).setFontColor(COLOR_WHITE))
                    .setBackgroundColor(new DeviceRgb(88, 80, 236))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(4);
            commitTable.addHeaderCell(headerCell);
        }

        int limit = Math.min(commits.size(), 50);
        for (int i = 0; i < limit; i++) {
            Map<String, Object> commit = commits.get(i);

            String dateStr = (String) commit.getOrDefault("date", "");
            String formattedDate = "";
            try {
                if (!dateStr.isEmpty()) {
                    String datePart = dateStr.length() > 19 ? dateStr.substring(0, 19) : dateStr;
                    LocalDateTime commitDate = LocalDateTime.parse(datePart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    formattedDate = commitDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                }
            } catch (Exception e) {
                formattedDate = dateStr;
            }

            String author = (String) commit.getOrDefault("author", "");
            String message = (String) commit.getOrDefault("message", "");
            if (message.contains("\n")) {
                message = message.substring(0, message.indexOf("\n")) + "...";
            }
            String sha = (String) commit.getOrDefault("sha", "");
            if (sha.length() > 7) {
                sha = sha.substring(0, 7);
            }

            commitTable.addCell(new Cell().add(new Paragraph(formattedDate).setFontSize(7)).setPadding(3));
            commitTable.addCell(new Cell().add(new Paragraph(author).setFontSize(7)).setPadding(3));
            commitTable.addCell(new Cell().add(new Paragraph(message).setFontSize(7)).setPadding(3));
            commitTable.addCell(new Cell().add(new Paragraph(sha).setFontSize(7).setFontColor(COLOR_PRIMARY)).setPadding(3));
        }

        if (commits.size() > 50) {
            document.add(new Paragraph("  ... và " + (commits.size() - 50) + " commits khác")
                    .setFontColor(COLOR_SLATE)
                    .setFontSize(8)
                    .setMarginTop(4));
        }

        document.add(commitTable);
        document.add(new Paragraph("\n"));
    }
}
package srpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.model.*;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IIssueRepository;
import srpm.repository.ISubmissionRepository;
import srpm.service.ISubmissionService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubmissionService implements ISubmissionService {

    private final ISubmissionRepository ISubmissionRepository;
    private final IIssueRepository IIssueRepository;
    private final IGroupMemberRepository IGroupMemberRepository;
    private final JiraIssuePushService jiraIssuePushService;
    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    public SubmissionService(
            ISubmissionRepository ISubmissionRepository,
            IIssueRepository IIssueRepository,
            IGroupMemberRepository IGroupMemberRepository,
            JiraIssuePushService jiraIssuePushService
    ) {
        this.ISubmissionRepository = ISubmissionRepository;
        this.IIssueRepository = IIssueRepository;
        this.IGroupMemberRepository = IGroupMemberRepository;
        this.jiraIssuePushService = jiraIssuePushService;
    }

    public Submission submitForIssue(
            Long issueId,
            Long groupMemberId,
            String content
    ) {
        logger.info("Sinh viên {} nộp bài cho Issue {}", groupMemberId, issueId);

        Issue issue = IIssueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue không tồn tại: " + issueId));

        if (issue.getStatus() == IssueStatus.DONE) {
            throw new RuntimeException("Issue đã hoàn thành, không thể nộp bài. Trạng thái hiện tại: " + issue.getStatus());
        }

        GroupMember submitter = IGroupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new RuntimeException("GroupMember không tồn tại: " + groupMemberId));

        if (!submitter.getGroup().getId().equals(issue.getGroup().getId())) {
            throw new RuntimeException(
                    "Sinh viên không thuộc nhóm của Issue này (submitterGroupId=" + submitter.getGroup().getId()
                    + ", issueGroupId=" + issue.getGroup().getId() + ")");
        }

        if (issue.getAssignedTo() == null) {
            throw new RuntimeException("Issue này chưa được giao cho ai. Chỉ người được giao mới có thể nộp bài.");
        }

        if (!issue.getAssignedTo().getId().equals(groupMemberId)) {
            throw new RuntimeException("Bạn không được giao Issue này. Chỉ người được giao mới có thể nộp bài.");
        }

        return saveSubmissionAndUpdateStatus(issue, submitter, content);
    }

    @Override
    @Transactional
    public Submission submitIssue(Long issueId, String content, Student student) {
        logger.info("Sinh viên {} (ID={}) nộp bài cho Issue {}", student.getUsername(), student.getID(), issueId);

        Issue issue = IIssueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue không tồn tại: " + issueId));

        // ✅ Tìm GroupMember của sinh viên thuộc đúng group của Issue
        var groupMember = IGroupMemberRepository.findByGroupAndStudent(issue.getGroup().getId(), student.getID())
                .orElseThrow(() -> new RuntimeException(
                        "Sinh viên không phải thành viên của nhóm (groupId=" + issue.getGroup().getId() + ") chứa Issue này"));

        return submitForIssue(issueId, groupMember.getId(), content);
    }

    private Submission saveSubmissionAndUpdateStatus(Issue issue, GroupMember submitter, String content) {
        String submissionCode = generateSubmissionCode();
        Submission submission = new Submission();
        submission.setSubmissionCode(submissionCode);
        submission.setIssue(issue);
        submission.setSubmittedBy(submitter);
        submission.setContent(content);
        submission.setSubmittedAt(LocalDateTime.now());

        submission = ISubmissionRepository.save(submission);
        logger.info("✓ Tạo Submission thành công: {}", submissionCode);

        issue.setStatus(IssueStatus.DONE);
        IIssueRepository.save(issue);
        logger.info("✓ Cập nhật Issue status thành DONE");

        if (issue.getIssueCode() != null && !issue.getIssueCode().isEmpty()) {
            try {
                jiraIssuePushService.updateJiraStatus(
                        issue.getIssueCode(),
                        "Done",
                        issue.getGroup().getJiraUrl(),
                        issue.getGroup().getJiraAdminEmail(),
                        issue.getGroup().getJiraApiToken()
                );
                logger.info("✓ Đồng bộ trạng thái sang Jira: {}", issue.getIssueCode());
            } catch (Exception e) {
                logger.warn("⚠️ Lỗi khi đồng bộ status sang Jira: {}", e.getMessage());
            }
        }

        return submission;
    }

    @Override
    public boolean isIssueSubmitted(Long issueId) {
        return !ISubmissionRepository.findByIssueId(issueId).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByGroup(Long groupId) {
        logger.debug("Lấy submissions cho group {}", groupId);
        return ISubmissionRepository.findByGroupId(groupId);
    }

    private String generateSubmissionCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 4);
        return "SUB-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }
}

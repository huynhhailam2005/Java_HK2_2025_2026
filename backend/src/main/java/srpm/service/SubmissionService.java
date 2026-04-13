package srpm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import srpm.model.*;
import srpm.repository.SubmissionRepository;
import srpm.repository.IssueRepository;
import srpm.repository.GroupMemberRepository;
import srpm.repository.AttachmentRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final IssueRepository issueRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final AttachmentRepository attachmentRepository;
    private final JiraIssuePushService jiraIssuePushService;
    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    public SubmissionService(
            SubmissionRepository submissionRepository,
            IssueRepository issueRepository,
            GroupMemberRepository groupMemberRepository,
            AttachmentRepository attachmentRepository,
            JiraIssuePushService jiraIssuePushService
    ) {
        this.submissionRepository = submissionRepository;
        this.issueRepository = issueRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.attachmentRepository = attachmentRepository;
        this.jiraIssuePushService = jiraIssuePushService;
    }

    /**
     * Sinh viên nộp bài cho một Issue
     *
     * @param issueId ID của Issue
     * @param groupMemberId ID của GroupMember (người nộp)
     * @param content Nội dung bài nộp (link hoặc text)
     * @param files Danh sách file đính kèm (tuỳ chọn)
     * @return Submission vừa được tạo
     */
    public Submission submitForIssue(
            Long issueId,
            Long groupMemberId,
            String content,
            MultipartFile[] files
    ) {
        logger.info("Sinh viên {} nộp bài cho Issue {}", groupMemberId, issueId);

        // ========== Validate Issue ==========
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue không tồn tại: " + issueId));

        if (issue.getIsDeleted()) {
            throw new RuntimeException("Issue đã bị xóa");
        }

        // ========== Validate trạng thái Issue ==========
        // Issue phải ở trạng thái TODO hoặc IN_PROGRESS để nộp bài (không được là DONE hay CANCELLED)
        if (issue.getStatus() == IssueStatus.DONE || issue.getStatus() == IssueStatus.CANCELLED) {
            throw new RuntimeException("Issue đã hoàn thành hoặc bị hủy, không thể nộp bài. Trạng thái hiện tại: " + issue.getStatus());
        }

        // ========== Validate GroupMember ==========
        GroupMember submitter = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new RuntimeException("GroupMember không tồn tại: " + groupMemberId));

        // Kiểm tra submitter thuộc cùng group với issue
        if (!submitter.getGroup().getId().equals(issue.getGroup().getId())) {
            throw new RuntimeException("Sinh viên không thuộc nhóm của Issue này");
        }

        // ========== AUTHORIZATION: Kiểm tra xem Issue có được giao cho submitter không ==========
        if (issue.getAssignedTo() == null) {
            throw new RuntimeException("Issue này chưa được giao cho ai. Chỉ người được giao mới có thể nộp bài.");
        }

        if (!issue.getAssignedTo().getId().equals(groupMemberId)) {
            throw new RuntimeException("Bạn không được giao Issue này. Chỉ người được giao mới có thể nộp bài.");
        }

        // ========== Tạo Submission ==========
        String submissionCode = generateSubmissionCode();

        Submission submission = new Submission();
        submission.setSubmissionCode(submissionCode);
        submission.setIssue(issue);
        submission.setSubmittedBy(submitter);
        submission.setContent(content);
        submission.setSubmittedAt(LocalDateTime.now());

        submission = submissionRepository.save(submission);
        logger.info("✓ Tạo Submission thành công: {}", submissionCode);

        // ========== Xử lý Files ==========
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        Attachment attachment = createAttachmentFromFile(file, submission, submitter);
                        logger.info("✓ Upload file thành công: {}", attachment.getFileName());
                    } catch (IOException e) {
                        logger.error("Lỗi upload file: {}", e.getMessage());
                        throw new RuntimeException("Lỗi upload file: " + e.getMessage());
                    }
                }
            }
        }

        // ========== Cập nhật Issue status ==========
        issue.setStatus(IssueStatus.DONE);
        issueRepository.save(issue);
        logger.info("✓ Cập nhật Issue status thành DONE");

        // ========== Push status sang Jira ==========
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
                // Không throw exception vì submission đã được tạo thành công
                // Chỉ log warning
            }
        }

        return submission;
    }

    /**
     * Tạo Attachment từ MultipartFile
     */
    private Attachment createAttachmentFromFile(
            MultipartFile file,
            Submission submission,
            GroupMember uploader
    ) throws IOException {
        // Generate file name
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String savedFileName = UUID.randomUUID().toString() + "." + extension;

        // TODO: Lưu file vào S3/local storage
        // Tạm thời: giả sử file được lưu
        String fileUrl = "/uploads/" + savedFileName;

        Attachment attachment = new Attachment();
        attachment.setAttachmentCode(generateAttachmentCode());
        attachment.setFileName(originalFileName);
        attachment.setFileUrl(fileUrl);
        attachment.setFileType(file.getContentType());
        attachment.setSubmission(submission);
        attachment.setUploader(uploader);
        attachment.setUploadedAt(LocalDateTime.now());

        attachment = attachmentRepository.save(attachment);
        return attachment;
    }

    /**
     * Lấy extension từ file name
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Sinh mã Submission: SUB-{TIMESTAMP}-{RANDOM}
     * Tổng độ dài: 4 + 8 + 1 + 4 = 17 character (dưới limit 20)
     */
    private String generateSubmissionCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 4);
        return "SUB-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    /**
     * Sinh mã Attachment: ATT-{RANDOM}
     * Tổng độ dài: 4 + 8 = 12 character (dưới limit 20)
     */
    private String generateAttachmentCode() {
        return "ATT-" + UUID.randomUUID().toString().substring(0, 8);
    }
}


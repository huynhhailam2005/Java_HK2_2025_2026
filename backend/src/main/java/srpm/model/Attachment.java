package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    @Column(name = "attachment_code", length = 20, nullable = false, unique = true)
    private String attachmentCode;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_member_id", nullable = false)
    private GroupMember uploader;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Attachment() {}

    public Attachment(String attachmentCode, String fileName, String fileUrl, String fileType) {
        this.attachmentCode = attachmentCode;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }

    public Long getId() { return id; }
    public String getAttachmentCode() { return attachmentCode; }
    public String getFileName() { return fileName; }
    public String getFileUrl() { return fileUrl; }
    public String getFileType() { return fileType; }
    public Issue getIssue() { return issue; }
    public Submission getSubmission() { return submission; }
    public GroupMember getUploader() { return uploader; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public void setId(Long id) { this.id = id; }
    public void setAttachmentCode(String attachmentCode) { this.attachmentCode = attachmentCode; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public void setSubmission(Submission submission) { this.submission = submission; }
    public void setUploader(GroupMember uploader) { this.uploader = uploader; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}


package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.CreateIssueRequest;
import srpm.model.*;
import srpm.repository.GroupMemberRepository;
import srpm.repository.GroupRepository;
import srpm.repository.IssueRepository;
import srpm.service.IIssueService;


@Service
@Transactional
public class IssueService implements IIssueService {

    private final IssueRepository issueDao;
    private final GroupRepository groupDao;
    private final GroupMemberRepository groupMemberDao;

    @Autowired
    public IssueService(IssueRepository issueDao,
                       GroupRepository groupDao,
                       GroupMemberRepository groupMemberDao) {
        this.issueDao = issueDao;
        this.groupDao = groupDao;
        this.groupMemberDao = groupMemberDao;
    }

    /**
     * Tạo Issue mới với validation các quy tắc:
     * - Epic phải có parentId = null
     * - SubTask phải có parentId không null (parent là Standard Issue hoặc Epic)
     * - Standard Issue (TASK, STORY, BUG) có thể có parentId = null hoặc parentId là Epic
     */
    public Issue createIssue(CreateIssueRequest request) {
        // 1. Validate Group tồn tại
        Group group = groupDao.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại: " + request.getGroupId()));

        // 2. Validate IssueType
        if (request.getIssueType() == null) {
            throw new RuntimeException("Loại Issue không được để trống");
        }

        // 3. Validate Title
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề Issue không được để trống");
        }

        // 4. Validate Parent ID theo quy tắc:
        Issue parentIssue = null;
        if (request.getIssueType() == IssueType.EPIC) {
            // Epic phải có parentId = null
            if (request.getParentId() != null) {
                throw new RuntimeException("Epic không thể có parent issue");
            }
        } else if (request.getIssueType() == IssueType.SUB_TASK) {
            // SubTask phải có parentId không null
            if (request.getParentId() == null) {
                throw new RuntimeException("SubTask phải có parent issue");
            }
            parentIssue = issueDao.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent Issue không tồn tại: " + request.getParentId()));

            // Validate parent của SubTask: không thể là SubTask, và phải cùng group
            if (parentIssue.getIssueType() == IssueType.SUB_TASK) {
                throw new RuntimeException("SubTask không thể có parent là SubTask");
            }
            if (!parentIssue.getGroup().getId().equals(group.getId())) {
                throw new RuntimeException("Parent Issue phải cùng nhóm");
            }
        } else {
            // Standard Issue (TASK, STORY, BUG)
            // Có thể có parentId = null hoặc parentId là Epic
            if (request.getParentId() != null) {
                parentIssue = issueDao.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent Issue không tồn tại: " + request.getParentId()));

                // Validate parent: phải là Epic
                if (parentIssue.getIssueType() != IssueType.EPIC) {
                    throw new RuntimeException("Standard Issue chỉ có thể có parent là Epic");
                }
                // Phải cùng group
                if (!parentIssue.getGroup().getId().equals(group.getId())) {
                    throw new RuntimeException("Parent Issue phải cùng nhóm");
                }
            }
        }

        // 5. Validate Assignee nếu có
        GroupMember assignedTo = null;
        if (request.getAssignedToMemberId() != null) {
            assignedTo = groupMemberDao.findById(request.getAssignedToMemberId())
                    .orElseThrow(() -> new RuntimeException("Thành viên nhóm không tồn tại: " + request.getAssignedToMemberId()));

            // Validate assignee thuộc group này
            if (!assignedTo.getGroup().getId().equals(group.getId())) {
                throw new RuntimeException("Thành viên phải thuộc nhóm này");
            }
        }

        // 6. Tạo Issue mới
        Issue issue = new Issue();
        issue.setGroup(group);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription() != null ? request.getDescription() : "");
        issue.setDeadline(request.getDeadline());
        issue.setIssueType(request.getIssueType());
        issue.setParent(parentIssue);
        issue.setAssignedTo(assignedTo);

        // Thiết lập syncStatus = PENDING (chưa sync lên Jira)
        issue.setSyncStatus(SyncStatus.PENDING);
        issue.setIsDeleted(false);

        // Lưu Issue vào database
        return issueDao.save(issue);
    }
}


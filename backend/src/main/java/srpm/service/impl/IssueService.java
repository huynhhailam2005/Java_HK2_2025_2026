package srpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.CreateIssueRequest;
import srpm.dto.request.UpdateIssueRequest;
import srpm.dto.IssueDetailDto;
import srpm.model.*;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IGroupRepository;
import srpm.repository.IIssueRepository;
import srpm.service.IIssueService;
import srpm.service.ISubmissionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class IssueService implements IIssueService {

    private static final Logger logger = LoggerFactory.getLogger(IssueService.class);

    private final IIssueRepository issueDao;
    private final IGroupRepository groupDao;
    private final IGroupMemberRepository groupMemberDao;
    private final ISubmissionService submissionService;
    private final JiraIssuePushService jiraIssuePushService;

    @Autowired
    public IssueService(IIssueRepository issueDao,
                        IGroupRepository groupDao,
                        IGroupMemberRepository groupMemberDao,
                        ISubmissionService submissionService,
                        JiraIssuePushService jiraIssuePushService) {
        this.issueDao = issueDao;
        this.groupDao = groupDao;
        this.groupMemberDao = groupMemberDao;
        this.submissionService = submissionService;
        this.jiraIssuePushService = jiraIssuePushService;
    }

    public Issue createIssue(CreateIssueRequest request) {
        Group group = groupDao.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại: " + request.getGroupId()));

        if (request.getIssueType() == null) {
            throw new RuntimeException("Loại Issue không được để trống");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề Issue không được để trống");
        }

        Issue parentIssue = null;
        if (request.getIssueType() == IssueType.EPIC) {
            if (request.getParentId() != null) {
                throw new RuntimeException("Epic không thể có parent issue");
            }
        } else if (request.getIssueType() == IssueType.SUB_TASK) {
            if (request.getParentId() == null) {
                throw new RuntimeException("SubTask phải có parent issue");
            }
            parentIssue = issueDao.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent Issue không tồn tại: " + request.getParentId()));

            if (parentIssue.getIssueType() == IssueType.EPIC) {
                throw new RuntimeException("SubTask không thể có parent là Epic");
            }
            if (parentIssue.getIssueType() == IssueType.SUB_TASK) {
                throw new RuntimeException("SubTask không thể có parent là SubTask");
            }
            if (!parentIssue.getGroup().getId().equals(group.getId())) {
                throw new RuntimeException("Parent Issue phải cùng nhóm");
            }
        } else {
            if (request.getParentId() != null) {
                parentIssue = issueDao.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent Issue không tồn tại: " + request.getParentId()));

                if (parentIssue.getIssueType() != IssueType.EPIC) {
                    throw new RuntimeException("Standard Issue chỉ có thể có parent là Epic");
                }

                if (!parentIssue.getGroup().getId().equals(group.getId())) {
                    throw new RuntimeException("Parent Issue phải cùng nhóm");
                }
            }
        }

        GroupMember assignedTo = null;
        if (request.getAssignedToMemberId() != null) {
            assignedTo = groupMemberDao.findById(request.getAssignedToMemberId())
                    .orElseThrow(() -> new RuntimeException("Thành viên nhóm không tồn tại: " + request.getAssignedToMemberId()));

            if (!assignedTo.getGroup().getId().equals(group.getId())) {
                throw new RuntimeException("Thành viên phải thuộc nhóm này");
            }
        }

        Issue issue = new Issue();
        issue.setGroup(group);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription() != null ? request.getDescription() : "");
        issue.setDeadline(request.getDeadlineAsLocalDateTime());
        issue.setIssueType(request.getIssueType());
        issue.setParent(parentIssue);
        issue.setAssignedTo(assignedTo);

        issue.setSyncStatus(SyncStatus.PENDING);

        return issueDao.save(issue);
    }

    @Override
    public Issue updateIssue(Long issueId, UpdateIssueRequest request) {
        Issue issue = issueDao.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            issue.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }

        if (request.getDeadline() != null && !request.getDeadline().isBlank()) {
            issue.setDeadline(request.getDeadlineAsLocalDateTime());
        } else if (request.getDeadline() != null && request.getDeadline().isBlank()) {
            issue.setDeadline(null);
        }

        if (request.getParentId() != null) {
            Issue parentIssue = issueDao.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent Issue không tồn tại: " + request.getParentId()));

            if (issue.getIssueType() == IssueType.EPIC && request.getParentId() != null) {
                throw new RuntimeException("Epic không thể có parent");
            }
            if (issue.getIssueType() == IssueType.SUB_TASK && parentIssue.getIssueType() == IssueType.EPIC) {
                throw new RuntimeException("SubTask không thể có parent là Epic");
            }
            if (issue.getIssueType() == IssueType.SUB_TASK && parentIssue.getIssueType() == IssueType.SUB_TASK) {
                throw new RuntimeException("SubTask không thể có parent là SubTask");
            }
            if (issue.getIssueType() != IssueType.SUB_TASK && issue.getIssueType() != IssueType.EPIC
                    && parentIssue.getIssueType() != IssueType.EPIC) {
                throw new RuntimeException("Standard Issue chỉ có thể có parent là Epic");
            }
            if (!parentIssue.getGroup().getId().equals(issue.getGroup().getId())) {
                throw new RuntimeException("Parent Issue phải cùng nhóm");
            }

            issue.setParent(parentIssue);
        } else if (request.getParentId() == null && request.getTitle() == null && request.getDescription() == null
                && request.getDeadline() == null && request.getAssignedToMemberId() == null && !request.isClearAssignee() && request.getStatus() == null) {
            throw new RuntimeException("Không có thông tin nào để cập nhật");
        }

        if (request.isClearAssignee()) {
            issue.setAssignedTo(null);
        } else if (request.getAssignedToMemberId() != null) {
            GroupMember newAssignee = groupMemberDao.findById(request.getAssignedToMemberId())
                    .orElseThrow(() -> new RuntimeException("Thành viên nhóm không tồn tại: " + request.getAssignedToMemberId()));

            if (!newAssignee.getGroup().getId().equals(issue.getGroup().getId())) {
                throw new RuntimeException("Thành viên phải thuộc cùng nhóm");
            }

            issue.setAssignedTo(newAssignee);
        }

        if (request.getStatus() != null) {
            try {
                IssueStatus newStatus = IssueStatus.fromValue(request.getStatus());
                issue.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái không hợp lệ: " + request.getStatus());
            }
        }

        return issueDao.save(issue);
    }

    @Override
    @Transactional
    public Map<String, Object> updateIssueStatus(Long issueId, String newStatusStr, User currentUser,
                                                  boolean isTeamLeader, boolean isAssignee) {
        Issue issue = issueDao.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

        if (!isTeamLeader && !isAssignee) {
            throw new RuntimeException("Bạn không có quyền cập nhật trạng thái Issue này. Chỉ nhóm trưởng/giảng viên hoặc người được giao mới được phép.");
        }

        IssueStatus newStatus;
        try {
            newStatus = IssueStatus.fromValue(newStatusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatusStr);
        }

        IssueStatus oldStatus = issue.getStatus();

        issue.setStatus(newStatus);
        issue = issueDao.save(issue);

        logger.info("✓ Cập nhật trạng thái Issue {} từ {} sang {}", issueId, oldStatus, newStatus);

        if (issue.getIssueCode() != null && !issue.getIssueCode().isEmpty()) {
            try {
                String jiraStatusName = mapLocalStatusToJira(newStatus);
                jiraIssuePushService.updateJiraStatus(
                        issue.getIssueCode(),
                        jiraStatusName,
                        issue.getGroup().getJiraUrl(),
                        issue.getGroup().getJiraAdminEmail(),
                        issue.getGroup().getJiraApiToken()
                );
                logger.info("✓ Đồng bộ trạng thái lên Jira: {}", issue.getIssueCode());
            } catch (Exception e) {
                logger.warn("⚠️ Lỗi khi đồng bộ trạng thái lên Jira: {}", e.getMessage());
            }
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("issueId", issue.getId());
        responseData.put("oldStatus", oldStatus);
        responseData.put("newStatus", newStatus);
        responseData.put("issueCode", issue.getIssueCode());

        return responseData;
    }

    private String mapLocalStatusToJira(IssueStatus status) {
        switch (status) {
            case DONE:         return "Done";
            case IN_PROGRESS:  return "In Progress";
            case TODO:         return "To Do";
            default:           return "To Do";
        }
    }

    @Override
    public List<IssueDetailDto> getIssuesByGroup(Long groupId) {
        List<Issue> issues = issueDao.findByGroupIdOrderByCreatedAtDesc(groupId);
        return issues.stream()
                .map(issue -> {
                    boolean hasSubmission = submissionService.isIssueSubmitted(issue.getId());
                    return IssueDetailDto.fromEntity(issue, hasSubmission);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<IssueDetailDto> getMyAssignedIssues(Long studentId) {
        List<Issue> issues = issueDao.findByAssignedToStudentId(studentId);
        return issues.stream()
                .map(issue -> {
                    boolean hasSubmission = submissionService.isIssueSubmitted(issue.getId());
                    return IssueDetailDto.fromEntity(issue, hasSubmission);
                })
                .collect(Collectors.toList());
    }
}


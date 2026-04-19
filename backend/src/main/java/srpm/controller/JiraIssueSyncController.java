package srpm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.CreateIssueRequest;
import srpm.dto.request.JiraIssueSyncRequest;
import srpm.dto.response.ApiResponse;
import srpm.model.*;
import srpm.service.impl.GroupAccessService;
import srpm.service.impl.IssueService;
import srpm.service.impl.JiraIssueSyncService;
import srpm.service.impl.JiraIssuePushService;
import srpm.repository.IssueRepository;
import srpm.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "http://localhost:5173")
public class JiraIssueSyncController {

    private static final Logger logger = LoggerFactory.getLogger(JiraIssueSyncController.class);

    private final JiraIssueSyncService jiraIssueSyncService;
    private final JiraIssuePushService jiraIssuePushService;
    private final IssueService issueService;
    private final GroupAccessService groupAccessService;
    private final IssueRepository issueDao;
    private final UserRepository userDao;

    @Autowired
    public JiraIssueSyncController(
            JiraIssueSyncService jiraIssueSyncService,
            JiraIssuePushService jiraIssuePushService,
            IssueService issueService,
            GroupAccessService groupAccessService,
            IssueRepository issueDao,
            UserRepository userDao
    ) {
        this.jiraIssueSyncService = jiraIssueSyncService;
        this.jiraIssuePushService = jiraIssuePushService;
        this.issueService = issueService;
        this.groupAccessService = groupAccessService;
        this.issueDao = issueDao;
        this.userDao = userDao;
    }

    /**
     * Sync issues từ Jira sang hệ thống (Pull - Batch)
     * POST /api/issues/sync-jira
     * Request body: { "groupId": 1, "projectKey": "PROJ" }
     */
    @PostMapping("/sync-jira")
    public ResponseEntity<ApiResponse> syncJiraIssues(@RequestBody JiraIssueSyncRequest request) {
        try {
            Map<String, Object> result = jiraIssueSyncService.syncJiraIssuesToLocalIssues(
                    request.getGroupId(),
                    request.getProjectKey()
            );

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    (String) result.get("message"),
                    result
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi khi đồng bộ issues từ Jira: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Tạo Issue mới trong SRPM
     * POST /api/issues
     * Request body: {
     *   "groupId": 1,
     *   "title": "Tên issue",
     *   "description": "Mô tả",
     *   "deadline": "2025-12-31T23:59:59",
     *   "issueType": "EPIC|TASK|STORY|BUG|SUB_TASK",
     *   "parentId": null,
     *   "assignedToMemberId": null
     * }
     *
     * Quy tắc tạo Issue:
     * - Epic: parentId phải = null
     * - SubTask: parentId phải không null (parent là Task/Story/BUG hoặc Epic)
     * - Standard Issue (Task/Story/BUG): parentId có thể null hoặc là Epic
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createIssue(@RequestBody CreateIssueRequest request) {
        try {
            // ✅ Kiểm tra authorization: chỉ Team Leader của group được phép
            if (!groupAccessService.isTeamLeaderOfGroup(request.getGroupId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Bạn không có quyền tạo Issue cho nhóm này. Chỉ nhóm trưởng hoặc giảng viên được phép.",
                        null
                ));
            }

            Issue createdIssue = issueService.createIssue(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                    true,
                    "Tạo Issue thành công",
                    Map.of(
                            "issueId", createdIssue.getId(),
                            "title", createdIssue.getTitle(),
                            "issueType", createdIssue.getIssueType(),
                            "syncStatus", createdIssue.getSyncStatus()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi tạo Issue: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Push Issue mới từ SRPM lên Jira
     * POST /api/issues/{issueId}/push-create
     * Yêu cầu: Issue phải thuộc group có cấu hình Jira
     * Authorization: Chỉ Team Leader của group được phép
     */
    @PostMapping("/{issueId}/push-create")
    public ResponseEntity<ApiResponse> createIssueOnJira(@PathVariable Long issueId) {
        try {
            Issue issue = issueDao.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

            // ✅ Kiểm tra authorization: chỉ Team Leader của group được phép
            if (!groupAccessService.isTeamLeaderOfGroup(issue.getGroup().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Bạn không có quyền push Issue này lên Jira. Chỉ nhóm trưởng hoặc giảng viên được phép.",
                        null
                ));
            }

            var group = issue.getGroup();
            if (group.getJiraUrl() == null || group.getJiraUrl().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Group chưa cấu hình Jira URL",
                        null
                ));
            }

            Issue createdIssue = jiraIssuePushService.createIssueOnJira(
                    issue,
                    group.getJiraUrl(),
                    group.getJiraProjectKey(),
                    group.getJiraAdminEmail(),
                    group.getJiraApiToken()
            );

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Tạo Issue thành công trên Jira với key: " + createdIssue.getIssueCode(),
                    Map.of(
                            "issueId", createdIssue.getId(),
                            "issueCode", createdIssue.getIssueCode(),
                            "syncStatus", createdIssue.getSyncStatus()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi tạo Issue trên Jira: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Push cập nhật Issue từ SRPM lên Jira
     * PUT /api/issues/{issueId}/push-update
     * Yêu cầu: Issue phải có issueCode (đã được sync trước đó)
     * Authorization: Chỉ Team Leader của group được phép
     */
    @PutMapping("/{issueId}/push-update")
    public ResponseEntity<ApiResponse> updateIssueOnJira(@PathVariable Long issueId) {
        try {
            Issue issue = issueDao.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

            // ✅ Kiểm tra authorization: chỉ Team Leader của group được phép
            if (!groupAccessService.isTeamLeaderOfGroup(issue.getGroup().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Bạn không có quyền cập nhật Issue này. Chỉ nhóm trưởng hoặc giảng viên được phép.",
                        null
                ));
            }

            var group = issue.getGroup();
            if (group.getJiraUrl() == null || group.getJiraUrl().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Group chưa cấu hình Jira URL",
                        null
                ));
            }

            Issue updatedIssue = jiraIssuePushService.updateIssueOnJira(
                    issue,
                    group.getJiraUrl(),
                    group.getJiraAdminEmail(),
                    group.getJiraApiToken()
            );

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Cập nhật Issue thành công trên Jira: " + updatedIssue.getIssueCode(),
                    Map.of(
                            "issueId", updatedIssue.getId(),
                            "issueCode", updatedIssue.getIssueCode(),
                            "syncStatus", updatedIssue.getSyncStatus()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi cập nhật Issue trên Jira: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Push xóa Issue từ SRPM lên Jira (Soft-Delete)
     * DELETE /api/issues/{issueId}/push-delete
     * Yêu cầu: Issue sẽ bị đánh dấu isDeleted=true, và xóa trên Jira nếu có issueCode
     * Authorization: Chỉ Team Leader của group được phép
     */
    @DeleteMapping("/{issueId}/push-delete")
    public ResponseEntity<ApiResponse> deleteIssueOnJira(@PathVariable Long issueId) {
        try {
            Issue issue = issueDao.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

            // ✅ Kiểm tra authorization: chỉ Team Leader của group được phép
            if (!groupAccessService.isTeamLeaderOfGroup(issue.getGroup().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Bạn không có quyền xóa Issue này. Chỉ nhóm trưởng hoặc giảng viên được phép.",
                        null
                ));
            }

            var group = issue.getGroup();
            if (group.getJiraUrl() == null || group.getJiraUrl().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Group chưa cấu hình Jira URL",
                        null
                ));
            }

            jiraIssuePushService.deleteIssueOnJira(
                    issue,
                    group.getJiraUrl(),
                    group.getJiraAdminEmail(),
                    group.getJiraApiToken()
            );

            			Issue deletedIssue = issueDao.findById(issueId)
            					.orElseThrow(() -> new RuntimeException("Không tìm thấy Issue sau khi xóa: " + issueId));
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Xóa Issue thành công (Soft-Delete)",
                    Map.of(
                            "issueId", deletedIssue.getId(),
                            "issueCode", deletedIssue.getIssueCode(),
                            "isDeleted", deletedIssue.getIsDeleted()
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi xóa Issue trên Jira: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Cập nhật trạng thái Issue
     * PATCH /api/issues/{issueId}/status
     * Authorization: Chỉ người được giao Issue hoặc Team Leader của group được phép
     * Request body: { "status": "IN_PROGRESS" | "COMPLETED" | "CANCELLED" }
     */
    @PatchMapping("/{issueId}/status")
    public ResponseEntity<ApiResponse> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody Map<String, String> request
    ) {
        try {
            Issue issue = issueDao.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

            String newStatusStr = request.get("status");
            if (newStatusStr == null || newStatusStr.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Trạng thái không được để trống",
                        null
                ));
            }

            // ========== AUTHORIZATION ==========
            // Lấy current user từ authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(
                        false,
                        "Chưa xác thực",
                        null
                ));
            }

            String username = authentication.getName();
            var userOptional = userDao.findByUsernameOrEmail(username, username);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(
                        false,
                        "User không tồn tại",
                        null
                ));
            }

            User user = userOptional.get();

            // Kiểm tra: Chỉ Team Leader của group HOẶC người được giao issue mới được phép
            boolean isTeamLeader = groupAccessService.isTeamLeaderOfGroup(issue.getGroup().getId());

            boolean isAssignee = false;
            if (user instanceof Student && issue.getAssignedTo() != null) {
                isAssignee = issue.getAssignedTo().getStudent().getId().equals(user.getID());
            }

            if (!isTeamLeader && !isAssignee) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Bạn không có quyền cập nhật trạng thái Issue này. Chỉ nhóm trưởng/giảng viên hoặc người được giao mới được phép.",
                        null
                ));
            }

            // ========== Validate trạng thái ==========
            IssueStatus newStatus;
            try {
                newStatus = IssueStatus.fromValue(newStatusStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Trạng thái không hợp lệ: " + newStatusStr,
                        null
                ));
            }

            IssueStatus oldStatus = issue.getStatus();

            // ========== Cập nhật trạng thái ==========
            issue.setStatus(newStatus);
            issue = issueDao.save(issue);

            logger.info("✓ Cập nhật trạng thái Issue {} từ {} sang {}", issueId, oldStatus, newStatus);

            // ========== Push status sang Jira nếu có issueCode ==========
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
                    // Không throw exception vì issue đã được update thành công trên SRPM
                }
            }

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Cập nhật trạng thái thành công: " + oldStatus + " → " + newStatus,
                    Map.of(
                            "issueId", issue.getId(),
                            "oldStatus", oldStatus,
                            "newStatus", newStatus,
                            "issueCode", issue.getIssueCode()
                    )
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi cập nhật trạng thái: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Map trạng thái local sang tên trạng thái Jira chuẩn
     */
    private String mapLocalStatusToJira(IssueStatus status) {
        switch (status) {
            case DONE:
                return "Done";
            case IN_PROGRESS:
                return "In Progress";
            case CANCELLED:
                return "Cancelled";
            case TODO:
            default:
                return "To Do";
        }
    }
}


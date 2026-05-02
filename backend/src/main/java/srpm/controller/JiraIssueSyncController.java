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
import srpm.dto.request.UpdateIssueRequest;
import srpm.dto.request.JiraIssueSyncRequest;
import srpm.dto.response.ApiResponse;
import srpm.model.*;
import srpm.service.impl.GroupAccessService;
import srpm.service.impl.IssueService;
import srpm.service.impl.JiraIssueSyncService;
import srpm.service.impl.JiraIssuePushService;
import srpm.repository.IIssueRepository;
import srpm.repository.IUserRepository;

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
    private final IIssueRepository issueDao;
    private final IUserRepository userDao;

    @Autowired
    public JiraIssueSyncController(
            JiraIssueSyncService jiraIssueSyncService,
            JiraIssuePushService jiraIssuePushService,
            IssueService issueService,
            GroupAccessService groupAccessService,
            IIssueRepository issueDao,
            IUserRepository userDao
    ) {
        this.jiraIssueSyncService = jiraIssueSyncService;
        this.jiraIssuePushService = jiraIssuePushService;
        this.issueService = issueService;
        this.groupAccessService = groupAccessService;
        this.issueDao = issueDao;
        this.userDao = userDao;
    }

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

    @PostMapping
    public ResponseEntity<ApiResponse> createIssue(@RequestBody CreateIssueRequest request) {
        try {
            if (!groupAccessService.isTeamLeaderOfGroup(request.getGroupId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Bạn không có quyền tạo Issue cho nhóm này. Chỉ nhóm trưởng hoặc giảng viên được phép.",
                        null
                ));
            }

            Issue createdIssue = issueService.createIssue(request);

            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("issueId", createdIssue.getId());
            responseData.put("title", createdIssue.getTitle());
            responseData.put("issueType", createdIssue.getIssueType());
            responseData.put("syncStatus", createdIssue.getSyncStatus());

            if (createdIssue.getParent() != null) {
                responseData.put("parentId", createdIssue.getParent().getId());
                responseData.put("parentCode", createdIssue.getParent().getIssueCode());
                responseData.put("parentTitle", createdIssue.getParent().getTitle());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                    true,
                    "Tạo Issue thành công",
                    responseData
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

    @PutMapping("/{issueId}")
    public ResponseEntity<ApiResponse> updateIssue(
            @PathVariable Long issueId,
            @RequestBody UpdateIssueRequest request
    ) {
        try {
            Issue issue = issueDao.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

            if (!groupAccessService.isTeamLeaderOfGroup(issue.getGroup().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Bạn không có quyền sửa Issue này. Chỉ nhóm trưởng hoặc giảng viên được phép.",
                        null
                ));
            }
            Issue updatedIssue = issueService.updateIssue(issueId, request);

            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("issueId", updatedIssue.getId());
            responseData.put("title", updatedIssue.getTitle());
            responseData.put("description", updatedIssue.getDescription());
            responseData.put("issueType", updatedIssue.getIssueType());
            responseData.put("status", updatedIssue.getStatus());
            responseData.put("deadline", updatedIssue.getDeadline());
            responseData.put("syncStatus", updatedIssue.getSyncStatus());

            if (updatedIssue.getParent() != null) {
                responseData.put("parentId", updatedIssue.getParent().getId());
                responseData.put("parentCode", updatedIssue.getParent().getIssueCode());
                responseData.put("parentTitle", updatedIssue.getParent().getTitle());
            }

            if (updatedIssue.getAssignedTo() != null) {
                responseData.put("assignedToMemberId", updatedIssue.getAssignedTo().getId());
                responseData.put("assignedTo", updatedIssue.getAssignedTo().getStudent().getUsername());
            }

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Cập nhật Issue thành công",
                    responseData
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
                    "Lỗi cập nhật Issue: " + e.getMessage(),
                    null
            ));
        }
    }

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

            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("issueId", createdIssue.getId());
            responseData.put("issueCode", createdIssue.getIssueCode());
            responseData.put("syncStatus", createdIssue.getSyncStatus());

            if (createdIssue.getParent() != null) {
                responseData.put("parentId", createdIssue.getParent().getId());
                responseData.put("parentCode", createdIssue.getParent().getIssueCode());
                responseData.put("parentTitle", createdIssue.getParent().getTitle());
            }

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Tạo Issue thành công trên Jira với key: " + createdIssue.getIssueCode(),
                    responseData
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

    @PutMapping("/{issueId}/push-update")
    public ResponseEntity<ApiResponse> updateIssueOnJira(@PathVariable Long issueId) {
        try {
            Issue issue = issueDao.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

            // Chặn nếu gọi Update mà chưa có mã Key Jira
            if (issue.getIssueCode() == null || issue.getIssueCode().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Issue này chưa được tạo trên Jira. Vui lòng sử dụng chức năng 'Push Create' (POST) trước!",
                        null
                ));
            }

            if (!groupAccessService.isTeamLeaderOfGroup(issue.getGroup().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Quyền truy cập bị từ chối", null));
            }

            Issue updated = jiraIssuePushService.updateIssueOnJira(
                    issue,
                    issue.getGroup().getJiraUrl(),
                    issue.getGroup().getJiraAdminEmail(),
                    issue.getGroup().getJiraApiToken()
            );

            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thành công!", updated.getIssueCode()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/{issueId}/status")
    public ResponseEntity<ApiResponse> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String newStatusStr = request.get("status");
            if (newStatusStr == null || newStatusStr.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Trạng thái không được để trống",
                        null
                ));
            }

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

            Issue issue = issueDao.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Issue: " + issueId));

            boolean isTeamLeader = groupAccessService.isTeamLeaderOfGroup(issue.getGroup().getId());
            boolean isAssignee = (user instanceof Student && issue.getAssignedTo() != null
                    && issue.getAssignedTo().getStudent().getId().equals(user.getID()));

            Map<String, Object> result = issueService.updateIssueStatus(
                    issueId, newStatusStr, user, isTeamLeader, isAssignee);

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Cập nhật trạng thái thành công: " + result.get("oldStatus") + " → " + result.get("newStatus"),
                    result
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false, "Lỗi cập nhật trạng thái: " + e.getMessage(), null));
        }
    }
}


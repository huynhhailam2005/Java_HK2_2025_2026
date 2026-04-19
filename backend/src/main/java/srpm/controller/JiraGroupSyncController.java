package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.JiraSyncRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.JiraGroupDto;
import srpm.service.impl.GroupAccessService;
import srpm.service.impl.JiraGroupSyncService;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class JiraGroupSyncController {

    private final JiraGroupSyncService jiraGroupSyncService;
    private final GroupAccessService groupAccessService;

    @Autowired
    public JiraGroupSyncController(JiraGroupSyncService jiraGroupSyncService, GroupAccessService groupAccessService) {
        this.jiraGroupSyncService = jiraGroupSyncService;
        this.groupAccessService = groupAccessService;
    }

    /**
     * Đồng bộ nhóm Jira sang nhóm trong hệ thống
     * POST /api/groups/{groupId}/sync-jira
     */
    @PostMapping("/{groupId}/sync-jira")
    public ResponseEntity<ApiResponse> syncJiraGroup(
            @PathVariable Long groupId,
            @RequestBody JiraSyncRequest request
    ) {
        // TODO: Kiểm tra quyền truy cập sẽ được bật sau khi debug
        /*
        if (!groupAccessService.canAccessGroup(groupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                    false,
                    "Bạn không có quyền truy cập nhóm này",
                    null
            ));
        }
        */

        try {
            JiraGroupDto jiraGroup = jiraGroupSyncService.syncJiraGroupToLocalGroup(
                    groupId,
                    request.getJiraGroupName()
            );

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Đồng bộ nhóm Jira thành công. Số thành viên: " + (jiraGroup.getMembers() != null ? jiraGroup.getMembers().size() : 0),
                    jiraGroup
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
                    "Lỗi khi đồng bộ nhóm Jira: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Lấy danh sách nhóm Jira
     * GET /api/groups/{groupId}/jira-groups
     */
    @GetMapping("/{groupId}/jira-groups")
    public ResponseEntity<ApiResponse> getJiraGroups(@PathVariable Long groupId) {
        // TODO: Kiểm tra quyền truy cập sẽ được bật sau khi debug
        /*
        if (!groupAccessService.canAccessGroup(groupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                    false,
                    "Bạn không có quyền truy cập nhóm này",
                    null
            ));
        }
        */

        try {
            List<String> groups = jiraGroupSyncService.getJiraGroups(groupId);

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Lấy danh sách nhóm Jira thành công",
                    groups
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
                    "Lỗi khi lấy danh sách nhóm Jira: " + e.getMessage(),
                    null
            ));
        }
    }
}


package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.JiraSyncRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.JiraGroupDto;
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

    @PostMapping("/{groupId}/sync-jira")
    public ResponseEntity<ApiResponse> syncJiraGroup(
            @PathVariable Long groupId,
            @RequestBody JiraSyncRequest request
    ) {
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
}


package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.dto.GroupMemberDto;
import srpm.model.GroupMember;
import srpm.model.User;
import srpm.service.IAuthorizationService;
import srpm.service.impl.GroupAccessService;
import srpm.service.impl.TeamLeaderService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class TeamLeaderController {

    private final TeamLeaderService teamLeaderService;
    private final GroupAccessService groupAccessService;
    private final IAuthorizationService authorizationService;

    @Autowired
    public TeamLeaderController(TeamLeaderService teamLeaderService,
                                 GroupAccessService groupAccessService,
                                 IAuthorizationService authorizationService) {
        this.teamLeaderService = teamLeaderService;
        this.groupAccessService = groupAccessService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/{groupId}/team-leader")
    public ResponseEntity<ApiResponse> assignTeamLeader(@PathVariable Long groupId,
                                                        @RequestBody Map<String, Long> request) {
        try {
            authorizationService.requireAuthentication();
            User user = authorizationService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            String role = user.getRole().name();
            if (!"LECTURER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền chỉ định nhóm trưởng. Chỉ giảng viên mới được phép.", null));
            }

            Long studentId = request.get("studentId");
            if (studentId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã sinh viên không được để trống", null));
            }

            GroupMember teamLeader = teamLeaderService.assignTeamLeader(groupId, studentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Chỉ định nhóm trưởng thành công", GroupMemberDto.fromEntity(teamLeader)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{groupId}/team-leader")
    public ResponseEntity<ApiResponse> changeTeamLeader(@PathVariable Long groupId,
                                                        @RequestBody Map<String, Long> request) {
        try {
            authorizationService.requireAuthentication();
            User user = authorizationService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            String role = user.getRole().name();
            if (!"LECTURER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền đổi nhóm trưởng. Chỉ giảng viên mới được phép.", null));
            }

            Long newStudentId = request.get("studentId");
            if (newStudentId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã sinh viên không được để trống", null));
            }

            GroupMember newTeamLeader = teamLeaderService.changeTeamLeader(groupId, newStudentId);
            return ResponseEntity.ok(new ApiResponse(true, "Đổi nhóm trưởng thành công", GroupMemberDto.fromEntity(newTeamLeader)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{groupId}/team-leader")
    public ResponseEntity<ApiResponse> getTeamLeader(@PathVariable Long groupId) {
        try {
            if (!groupAccessService.canAccessGroup(groupId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền xem nhóm trưởng của nhóm này", null));
            }

            Optional<GroupMember> teamLeader = teamLeaderService.getTeamLeader(groupId);
            if (teamLeader.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "OK", GroupMemberDto.fromEntity(teamLeader.get())));
            } else {
                return ResponseEntity.ok(new ApiResponse(true, "Nhóm chưa có nhóm trưởng", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{groupId}/team-leader")
    public ResponseEntity<ApiResponse> removeTeamLeader(@PathVariable Long groupId) {
        try {
            authorizationService.requireAuthentication();
            User user = authorizationService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            String role = user.getRole().name();
            if (!"LECTURER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền xóa nhóm trưởng. Chỉ giảng viên mới được phép.", null));
            }

            boolean removed = teamLeaderService.removeTeamLeader(groupId);
            if (removed) {
                return ResponseEntity.ok(new ApiResponse(true, "Xóa chỉ định nhóm trưởng thành công", null));
            } else {
                return ResponseEntity.ok(new ApiResponse(true, "Nhóm không có nhóm trưởng để xóa", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}


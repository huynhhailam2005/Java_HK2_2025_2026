package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.dto.GroupMemberDto;
import srpm.model.User;
import srpm.repository.IGroupMemberRepository;
import srpm.service.IAuthorizationService;
import srpm.service.IGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupMemberController {

    private final IGroupService groupService;
    private final IGroupMemberRepository IGroupMemberRepository;
    private final IAuthorizationService authorizationService;

    @Autowired
    public GroupMemberController(IGroupService groupService,
                                  IGroupMemberRepository IGroupMemberRepository,
                                  IAuthorizationService authorizationService) {
        this.groupService = groupService;
        this.IGroupMemberRepository = IGroupMemberRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse> getMembers(@PathVariable Long groupId) {
        try {
            List<GroupMemberDto> members = IGroupMemberRepository.findByGroup(groupId)
                    .stream()
                    .map(GroupMemberDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(new ApiResponse(true, "OK", members));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{groupId}/members/{studentId}")
    public ResponseEntity<ApiResponse> addMember(@PathVariable Long groupId,
                                                  @PathVariable Long studentId) {
        try {
            authorizationService.requireAuthentication();
            User user = authorizationService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            String role = user.getRole().name();
            if (!"LECTURER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền thêm thành viên. Chỉ giảng viên mới được phép.", null));
            }

            var data = groupService.addStudent(groupId, studentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Thêm thành viên thành công", data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse> removeMember(@PathVariable Long groupId,
                                                     @PathVariable Long memberId) {
        try {
            authorizationService.requireAuthentication();
            User user = authorizationService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            String role = user.getRole().name();
            if (!"LECTURER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền xóa thành viên. Chỉ giảng viên mới được phép.", null));
            }

            groupService.removeMember(groupId, memberId);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa thành viên thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}

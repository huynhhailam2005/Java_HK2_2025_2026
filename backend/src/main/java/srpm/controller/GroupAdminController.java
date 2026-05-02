package srpm.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.dto.response.ApiResponse;
import srpm.exception.ForbiddenException;
import srpm.exception.ResourceNotFoundException;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;
import srpm.service.IAuthorizationService;
import srpm.service.IGroupService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/groups")
@CrossOrigin
public class GroupAdminController {

    private static final Logger logger = LoggerFactory.getLogger(GroupAdminController.class);
    private final IGroupService groupService;
    private final IAuthorizationService authorizationService;

    @Autowired
    public GroupAdminController(IGroupService groupService, IAuthorizationService authorizationService) {
        this.groupService = groupService;
        this.authorizationService = authorizationService;
    }

    private ResponseEntity<ApiResponse> okResponse(String message, Object data) {
        return ResponseEntity.ok(new ApiResponse(true, message, data));
    }

    private ResponseEntity<ApiResponse> createdResponse(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, message, data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getGroup(@PathVariable Long id) {
        var groupDto = groupService.getGroupById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));

        User currentUser = authorizationService.getCurrentUser().orElse(null);
        boolean hasAccess = false;

        if (currentUser instanceof Lecturer lecturer) {
            hasAccess = groupDto.getLecturerId().equals(lecturer.getID());
        } else if (currentUser instanceof Student student) {
            hasAccess = groupDto.getMembers().stream()
                    .anyMatch(member -> member.getId().equals(student.getID()));
        } else {
            hasAccess = true;
        }

        if (!hasAccess) {
            logger.warn("Access denied for user: {} to group: {}",
                    currentUser != null ? currentUser.getUsername() : "unknown", id);
            throw new ForbiddenException("Bạn không có quyền xem group này");
        }

        return okResponse("OK", groupDto);
    }

    @PatchMapping("/{id}/lecturer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateGroupLecturer(@PathVariable Long id,
                                                           @RequestBody Map<String, Long> request) {
        Long lecturerId = request.get("lecturerId");
        if (lecturerId == null) {
            throw new ForbiddenException("Mã giảng viên không được để trống");
        }

        var dto = groupService.updateGroupLecturer(id, lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));

        logger.info("Group lecturer updated: {}", id);
        return okResponse("Cập nhật giảng viên group thành công", dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateGroupInfo(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateGroupRequest request) {
        var dto = groupService.updateGroupInfo(id, request)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));

        logger.info("Group info updated: {}", id);
        return okResponse("Cập nhật thông tin group thành công", dto);
    }
}


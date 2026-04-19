package srpm.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.GroupDto;
import srpm.exception.ForbiddenException;
import srpm.exception.ResourceNotFoundException;
import srpm.model.Group;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;
import srpm.repository.UserRepository;
import srpm.service.IGroupService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/groups")
@CrossOrigin
public class GroupAdminController {

    private static final Logger logger = LoggerFactory.getLogger(GroupAdminController.class);
    private final IGroupService groupService;
    private final UserRepository userDao;

    @Autowired
    public GroupAdminController(IGroupService groupService, UserRepository userDao) {
        this.groupService = groupService;
        this.userDao = userDao;
    }

    private ResponseEntity<ApiResponse> okResponse(String message, Object data) {
        return ResponseEntity.ok(new ApiResponse(true, message, data));
    }

    private ResponseEntity<ApiResponse> createdResponse(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, message, data));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userDao.findByUsername(username).orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllGroups() {
        User currentUser = getCurrentUser();
        logger.debug("Fetching groups for user: {}", currentUser != null ? currentUser.getUsername() : "unknown");

        List<Group> groups;
        if (currentUser instanceof Lecturer lecturer) {
            groups = groupService.getGroupsByLecturer(lecturer.getID());
        } else if (currentUser instanceof Student student) {
            groups = groupService.getGroupsByStudent(student.getID());
        } else {
            groups = groupService.getAllGroups();
        }

        List<GroupDto> groupDtos = groups.stream().map(GroupDto::fromEntity).toList();
        return okResponse("OK", groupDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getGroup(@PathVariable Long id) {
        Group group = groupService.getGroupById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));

        User currentUser = getCurrentUser();
        boolean hasAccess = false;

        if (currentUser instanceof Lecturer lecturer) {
            hasAccess = group.getLecturer().getID().equals(lecturer.getID());
        } else if (currentUser instanceof Student student) {
            hasAccess = group.getGroupMembers().stream()
                    .anyMatch(member -> member.getStudent().getID().equals(student.getID()));
        } else {
            hasAccess = true;
        }

        if (!hasAccess) {
            logger.warn("Access denied for user: {} to group: {}",
                    currentUser != null ? currentUser.getUsername() : "unknown", id);
            throw new ForbiddenException("Bạn không có quyền xem group này");
        }

        GroupDto dto = GroupDto.fromEntity(group);
        return okResponse("OK", dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        logger.debug("Creating new group: {}", request.getGroupName());
        Group group = groupService.createGroup(request);
        GroupDto dto = GroupDto.fromEntity(group);
        logger.info("Group created successfully: {}", group.getId());
        return createdResponse("Tạo group thành công", dto);
    }

    @PatchMapping("/{id}/name")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateGroupName(@PathVariable Long id,
                                                       @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            throw new ForbiddenException("Tên group không được để trống");
        }

        Group group = groupService.updateGroupName(id, newName)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));

        GroupDto dto = GroupDto.fromEntity(group);
        logger.info("Group name updated: {}", id);
        return okResponse("Cập nhật tên group thành công", dto);
    }

    @PatchMapping("/{id}/lecturer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateGroupLecturer(@PathVariable Long id,
                                                           @RequestBody Map<String, Long> request) {
        Long lecturerId = request.get("lecturerId");
        if (lecturerId == null) {
            throw new ForbiddenException("Mã giảng viên không được để trống");
        }

        Group group = groupService.updateGroupLecturer(id, lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));

        GroupDto dto = GroupDto.fromEntity(group);
        logger.info("Group lecturer updated: {}", id);
        return okResponse("Cập nhật giảng viên group thành công", dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateGroupInfo(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateGroupRequest request) {
        Group group = groupService.updateGroupInfo(id, request)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));

        GroupDto dto = GroupDto.fromEntity(group);
        logger.info("Group info updated: {}", id);
        return okResponse("Cập nhật thông tin group thành công", dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable Long id) {
        boolean deleted = groupService.deleteGroup(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Không tìm thấy group");
        }
        logger.info("Group deleted: {}", id);
        return okResponse("Xóa group thành công", null);
    }
}


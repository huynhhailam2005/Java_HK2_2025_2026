package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.GroupDto;
import srpm.model.Group;
import srpm.service.GroupAccessService;
import srpm.service.GroupService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final GroupService groupService;
    private final GroupAccessService groupAccessService;

    @Autowired
    public GroupController(GroupService groupService, GroupAccessService groupAccessService) {
        this.groupService = groupService;
        this.groupAccessService = groupAccessService;
    }

    /** GET /api/groups — Lấy tất cả groups */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllGroups() {
        try {
            List<Group> groups = groupService.getAllGroups();
            List<GroupDto> groupDtos = groups.stream().map(GroupDto::fromEntity).toList();
            return ResponseEntity.ok(new ApiResponse(true, "OK", groupDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** GET /api/groups/{groupId} — Chi tiết group */
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse> getGroupById(@PathVariable Long groupId) {
        try {
            Optional<Group> group = groupService.getGroupById(groupId);
            if (group.isPresent()) {
                GroupDto groupDto = GroupDto.fromEntity(group.get());
                return ResponseEntity.ok(new ApiResponse(true, "OK", groupDto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** POST /api/groups — Tạo group mới */
    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(@RequestBody GroupRequest req) {
        try {
            Group group = groupService.createGroup(req);
            GroupDto groupDto = GroupDto.fromEntity(group);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tạo group thành công", groupDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** PUT /api/groups/{groupId} — Cập nhật tên group */
    @PatchMapping("/{groupId}/name")
    public ResponseEntity<ApiResponse> updateGroupName(@PathVariable Long groupId,
                                                       @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("name");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Tên group không được để trống", null));
            }

            Optional<Group> group = groupService.updateGroupName(groupId, newName);
            if (group.isPresent()) {
                GroupDto groupDto = GroupDto.fromEntity(group.get());
                return ResponseEntity.ok(new ApiResponse(true, "Cập nhật tên group thành công", groupDto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** PATCH /api/groups/{groupId}/lecturer — Cập nhật giảng viên cho group */
    @PatchMapping("/{groupId}/lecturer")
    public ResponseEntity<ApiResponse> updateGroupLecturer(@PathVariable Long groupId,
                                                           @RequestBody Map<String, Long> request) {
        try {
            Long lecturerId = request.get("lecturerId");
            if (lecturerId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã giảng viên không được để trống", null));
            }

            Optional<Group> group = groupService.updateGroupLecturer(groupId, lecturerId);
            if (group.isPresent()) {
                GroupDto groupDto = GroupDto.fromEntity(group.get());
                return ResponseEntity.ok(new ApiResponse(true, "Cập nhật giảng viên group thành công", groupDto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** DELETE /api/groups/{groupId} — Xóa group */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable Long groupId) {
        try {
            boolean deleted = groupService.deleteGroup(groupId);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "Xóa group thành công", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** POST /api/groups/{groupId}/students/{studentId} — Thêm student vào group */
    @Transactional
    @PostMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> addStudent(@PathVariable Long groupId,
                                                  @PathVariable Long studentId) {
        try {
            if (!groupAccessService.canAccessGroup(groupId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền thêm sinh viên vào nhóm này", null));
            }

            Group group = groupService.addStudent(groupId, studentId);
            GroupDto groupDto = GroupDto.fromEntity(group);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Thêm student vào group thành công", groupDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** DELETE /api/groups/{groupId}/students/{studentId} — Xóa student khỏi group */
    @Transactional
    @DeleteMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> removeStudent(@PathVariable Long groupId,
                                                     @PathVariable Long studentId) {
        try {
            if (!groupAccessService.canAccessGroup(groupId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền xóa sinh viên khỏi nhóm này", null));
            }

            boolean removed = groupService.removeStudent(groupId, studentId);
            if (removed) {
                return ResponseEntity.ok(new ApiResponse(true, "Xóa student khỏi group thành công", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy student trong group", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** PUT /api/groups/{groupId} — Cập nhật toàn bộ thông tin group (bao gồm Jira config) */
    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse> updateGroupInfo(@PathVariable Long groupId,
                                                       @RequestBody UpdateGroupRequest request) {
        try {
            Optional<Group> group = groupService.updateGroupInfo(groupId, request);
            if (group.isPresent()) {
                GroupDto groupDto = GroupDto.fromEntity(group.get());
                return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin group thành công", groupDto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group", null));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}


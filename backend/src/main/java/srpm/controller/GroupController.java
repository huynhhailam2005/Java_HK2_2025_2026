package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.GroupRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.GroupDto;
import srpm.model.Group;
import srpm.service.GroupService;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /** GET /api/groups — Lấy tất cả group */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllGroups() {
        List<GroupDto> groups = groupService.getAllGroups()
                .stream().map(GroupDto::fromEntity).toList();
        return ResponseEntity.ok(new ApiResponse(true, "OK", groups));
    }

    /** GET /api/groups/{id} — Lấy chi tiết group */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getGroupById(@PathVariable String id) {
        return groupService.getGroupById(id)
                .map(g -> ResponseEntity.ok(new ApiResponse(true, "OK", GroupDto.fromEntity(g))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group: " + id, null)));
    }

    /** POST /api/groups — Tạo group mới */
    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(@RequestBody GroupRequest req) {
        try {
            Group group = groupService.createGroup(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tạo group thành công", GroupDto.fromEntity(group)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** PUT /api/groups/{id} — Cập nhật group */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateGroup(@PathVariable String id,
                                                   @RequestBody GroupRequest req) {
        try {
            return groupService.updateGroup(id, req)
                    .map(g -> ResponseEntity.ok(new ApiResponse(true, "Cập nhật thành công", GroupDto.fromEntity(g))))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Không tìm thấy group: " + id, null)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** DELETE /api/groups/{id} — Xoá group */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable String id) {
        if (groupService.deleteGroup(id)) {
            return ResponseEntity.ok(new ApiResponse(true, "Xoá group thành công", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Không tìm thấy group: " + id, null));
    }

    /** POST /api/groups/{groupId}/students/{studentId} — Thêm sinh viên vào group */
    @PostMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> addStudent(@PathVariable String groupId,
                                                  @PathVariable String studentId) {
        try {
            return groupService.addStudent(groupId, studentId)
                    .map(g -> ResponseEntity.ok(new ApiResponse(true, "Thêm sinh viên thành công", GroupDto.fromEntity(g))))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Không tìm thấy group: " + groupId, null)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /** DELETE /api/groups/{groupId}/students/{studentId} — Xoá sinh viên khỏi group */
    @DeleteMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> removeStudent(@PathVariable String groupId,
                                                     @PathVariable String studentId) {
        return groupService.removeStudent(groupId, studentId)
                .map(g -> ResponseEntity.ok(new ApiResponse(true, "Xoá sinh viên thành công", GroupDto.fromEntity(g))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group: " + groupId, null)));
    }
}

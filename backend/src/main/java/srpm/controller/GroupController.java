package srpm.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.GroupDto;
import srpm.exception.ForbiddenException;
import srpm.exception.ResourceNotFoundException;
import srpm.model.Group;
import srpm.service.IGroupAccessService;
import srpm.service.IGroupService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final IGroupService groupService;
    private final IGroupAccessService groupAccessService;

    @Autowired
    public GroupController(IGroupService groupService, IGroupAccessService groupAccessService) {
        this.groupService = groupService;
        this.groupAccessService = groupAccessService;
    }

    private ResponseEntity<ApiResponse> okResponse(String message, Object data) {
        return ResponseEntity.ok(new ApiResponse(true, message, data));
    }

    private ResponseEntity<ApiResponse> createdResponse(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, message, data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        List<GroupDto> groupDtos = groups.stream().map(GroupDto::fromEntity).toList();
        return okResponse("OK", groupDtos);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse> getGroupById(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));
        GroupDto groupDto = GroupDto.fromEntity(group);
        return okResponse("OK", groupDto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(@Valid @RequestBody GroupRequest req) {
        Group group = groupService.createGroup(req);
        GroupDto groupDto = GroupDto.fromEntity(group);
        return createdResponse("Tạo group thành công", groupDto);
    }

    @PatchMapping("/{groupId}/name")
    public ResponseEntity<ApiResponse> updateGroupName(@PathVariable Long groupId,
                                                       @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            throw new ForbiddenException("Tên group không được để trống");
        }

        Group group = groupService.updateGroupName(groupId, newName)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));
        GroupDto groupDto = GroupDto.fromEntity(group);
        return okResponse("Cập nhật tên group thành công", groupDto);
    }

    @PatchMapping("/{groupId}/lecturer")
    public ResponseEntity<ApiResponse> updateGroupLecturer(@PathVariable Long groupId,
                                                           @RequestBody Map<String, Long> request) {
        Long lecturerId = request.get("lecturerId");
        if (lecturerId == null) {
            throw new ForbiddenException("Mã giảng viên không được để trống");
        }

        Group group = groupService.updateGroupLecturer(groupId, lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));
        GroupDto groupDto = GroupDto.fromEntity(group);
        return okResponse("Cập nhật giảng viên group thành công", groupDto);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse> updateGroupInfo(@PathVariable Long groupId,
                                                       @Valid @RequestBody UpdateGroupRequest request) {
        Group group = groupService.updateGroupInfo(groupId, request)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group"));
        GroupDto groupDto = GroupDto.fromEntity(group);
        return okResponse("Cập nhật thông tin group thành công", groupDto);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable Long groupId) {
        boolean deleted = groupService.deleteGroup(groupId);
        if (!deleted) {
            throw new ResourceNotFoundException("Không tìm thấy group");
        }
        return okResponse("Xóa group thành công", null);
    }

    @Transactional
    @PostMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> addStudent(@PathVariable Long groupId,
                                                  @PathVariable Long studentId) {
        if (!groupAccessService.canAccessGroup(groupId)) {
            throw new ForbiddenException("Bạn không có quyền thêm sinh viên vào nhóm này");
        }

        Group group = groupService.addStudent(groupId, studentId);
        GroupDto groupDto = GroupDto.fromEntity(group);
        return createdResponse("Thêm student vào group thành công", groupDto);
    }

    @Transactional
    @DeleteMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> removeStudent(@PathVariable Long groupId,
                                                     @PathVariable Long studentId) {
        if (!groupAccessService.canAccessGroup(groupId)) {
            throw new ForbiddenException("Bạn không có quyền xóa sinh viên khỏi nhóm này");
        }

        boolean removed = groupService.removeStudent(groupId, studentId);
        if (!removed) {
            throw new ResourceNotFoundException("Không tìm thấy student trong group");
        }
        return okResponse("Xóa student khỏi group thành công", null);
    }
}


package srpm.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.dto.response.ApiResponse;
import srpm.exception.ResourceNotFoundException;
import srpm.model.User;
import srpm.service.IAuthorizationService;
import srpm.service.IGroupService;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final IGroupService groupService;
    private final IAuthorizationService authorizationService;

    @Autowired
    public GroupController(IGroupService groupService, IAuthorizationService authorizationService) {
        this.groupService = groupService;
        this.authorizationService = authorizationService;
    }

    private ResponseEntity<ApiResponse> okResponse(String message, Object data) {
        return ResponseEntity.ok(new ApiResponse(true, message, data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllGroups() {
        return okResponse("OK", groupService.getAllGroups());
    }

    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<ApiResponse> getGroupsByLecturer(@PathVariable Long lecturerId) {
        return okResponse("Thành công", groupService.getGroupsByLecturer(lecturerId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse> getGroupsByStudent(@PathVariable Long studentId) {
        return okResponse("Thành công", groupService.getGroupsByStudent(studentId));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse> getGroupById(@PathVariable Long groupId) {
        return okResponse("OK", groupService.getGroupById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(@Valid @RequestBody GroupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Thành công", groupService.createGroup(req)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse> updateGroupInfo(@PathVariable Long groupId, @Valid @RequestBody UpdateGroupRequest request) {
        return okResponse("Thành công", groupService.updateGroupInfo(groupId, request)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy group")));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return okResponse("Xóa thành công", null);
    }
}
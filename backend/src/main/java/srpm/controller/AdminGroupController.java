package srpm.controller;

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
import srpm.model.Group;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;
import srpm.repository.UserRepository;
import srpm.service.GroupService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/groups")
@CrossOrigin
public class AdminGroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    @Autowired
    public AdminGroupController(GroupService groupService, UserRepository userRepository) {
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<ApiResponse> getAllGroups() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);

            List<Group> groups;
            if (currentUser instanceof Lecturer lecturer) {
                // Lecturer xem groups mà họ quản lý
                groups = groupService.getGroupsByLecturer(lecturer.getID());
            } else if (currentUser instanceof Student student) {
                // Student xem groups mà họ là thành viên
                groups = groupService.getGroupsByStudent(student.getID());
            } else {
                // Admin xem tất cả groups
                groups = groupService.getAllGroups();
            }

            List<GroupDto> groupDtos = groups.stream().map(GroupDto::fromEntity).toList();
            return ResponseEntity.ok(new ApiResponse(true, "OK", groupDtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<?> getGroup(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);

            Group group = groupService.getGroupById(id).orElse(null);
            if (group == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Không tìm thấy group", null));
            }

            // Check authorization
            boolean hasAccess = false;
            if (currentUser instanceof Lecturer lecturer) {
                hasAccess = group.getLecturer().getID().equals(lecturer.getID());
            } else if (currentUser instanceof Student student) {
                hasAccess = group.getGroupMembers().stream()
                        .anyMatch(member -> member.getStudent().getID().equals(student.getID()));
            } else {
                hasAccess = true; // Admin
            }

            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Bạn không có quyền xem group này", null));
            }

            GroupDto dto = GroupDto.fromEntity(group);
            return ResponseEntity.ok(new ApiResponse(true, "OK", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody GroupRequest request) {
        try {
            Group group = groupService.createGroup(request);
            GroupDto dto = GroupDto.fromEntity(group);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tạo group thành công", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }


    @PatchMapping("/{id}/name")
    public ResponseEntity<?> updateGroupName(@PathVariable Long id,
                                             @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("name");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Tên group không được để trống", null));
            }

            return groupService.updateGroupName(id, newName)
                    .map(group -> {
                        GroupDto dto = GroupDto.fromEntity(group);
                        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật tên group thành công", dto));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Không tìm thấy group", null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/lecturer")
    public ResponseEntity<?> updateGroupLecturer(@PathVariable Long id,
                                                 @RequestBody Map<String, Long> request) {
        try {
            Long lecturerId = request.get("lecturerId");
            if (lecturerId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã giảng viên không được để trống", null));
            }

            return groupService.updateGroupLecturer(id, lecturerId)
                    .map(group -> {
                        GroupDto dto = GroupDto.fromEntity(group);
                        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật giảng viên group thành công", dto));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Không tìm thấy group", null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Cập nhật toàn bộ thông tin group (tên, giảng viên, Jira, GitHub)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroupInfo(@PathVariable Long id,
                                            @RequestBody UpdateGroupRequest request) {
        try {
            // Kiểm tra xem có ít nhất một field để update
            if (request.getGroupName() == null && request.getLecturerId() == null &&
                request.getJiraUrl() == null && request.getJiraProjectKey() == null &&
                request.getJiraApiToken() == null && request.getJiraAdminEmail() == null &&
                request.getGithubRepoUrl() == null && request.getGithubAccessToken() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Phải cung cấp ít nhất một thông tin để cập nhật", null));
            }

            return groupService.updateGroupInfo(id, request)
                    .map(group -> {
                        GroupDto dto = GroupDto.fromEntity(group);
                        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin group thành công", dto));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Không tìm thấy group", null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Cập nhật thông tin Jira cho group
     */
    @PatchMapping("/{id}/jira")
    public ResponseEntity<?> updateGroupJiraInfo(@PathVariable Long id,
                                                @RequestBody Map<String, String> request) {
        try {
            UpdateGroupRequest updateRequest = new UpdateGroupRequest();
            updateRequest.setJiraUrl(request.get("jiraUrl"));
            updateRequest.setJiraProjectKey(request.get("jiraProjectKey"));
            updateRequest.setJiraApiToken(request.get("jiraApiToken"));
            updateRequest.setJiraAdminEmail(request.get("jiraAdminEmail"));

            return groupService.updateGroupInfo(id, updateRequest)
                    .map(group -> {
                        GroupDto dto = GroupDto.fromEntity(group);
                        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin Jira thành công", dto));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Không tìm thấy group", null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Cập nhật thông tin GitHub cho group
     */
    @PatchMapping("/{id}/github")
    public ResponseEntity<?> updateGroupGithubInfo(@PathVariable Long id,
                                                  @RequestBody Map<String, String> request) {
        try {
            UpdateGroupRequest updateRequest = new UpdateGroupRequest();
            updateRequest.setGithubRepoUrl(request.get("githubRepoUrl"));
            updateRequest.setGithubAccessToken(request.get("githubAccessToken"));

            return groupService.updateGroupInfo(id, updateRequest)
                    .map(group -> {
                        GroupDto dto = GroupDto.fromEntity(group);
                        return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin GitHub thành công", dto));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Không tìm thấy group", null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        boolean deleted = groupService.deleteGroup(id);
        if (deleted) return ResponseEntity.ok("Xóa group thành công");
        return ResponseEntity.notFound().build();
    }
}
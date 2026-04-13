package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.AdminUserManagementRequest;
import srpm.dto.response.AdminUserManagementResponse;
import srpm.dto.response.ApiResponse;
import srpm.model.Admin;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;
import srpm.service.AdminUserManagementService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @Autowired
    public AdminUserManagementController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUsers(@RequestParam(required = false) String role) {
        try {
            List<AdminUserManagementResponse> users = adminUserManagementService.getManagedUsers(role)
                    .stream()
                    .map(this::toResponse)
                    .toList();

            String message;
            if (role != null && !role.isBlank()) {
                message = "Lấy tất cả " + role.toLowerCase() + " thành công";
            } else {
                message = "Lấy danh sách người dùng thành công";
            }

            return ResponseEntity.ok(new ApiResponse(true, message, users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        try {
            User user = adminUserManagementService.getManagedUserById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin người dùng thành công", toResponse(user)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody AdminUserManagementRequest request) {
        try {
            User user = adminUserManagementService.updateManagedUser(id, request);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật người dùng thành công", toResponse(user)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        try {
            adminUserManagementService.deleteManagedUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa người dùng thành công", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    private AdminUserManagementResponse toResponse(User user) {
        String studentCode = null;
        String lecturerCode = null;
        String jiraAccountId = null;
        String githubUsername = null;

        if (user instanceof Student student) {
            studentCode = student.getStudentCode();
            jiraAccountId = student.getJiraAccountId();
            githubUsername = student.getGithubUsername();
        } else if (user instanceof Lecturer lecturer) {
            lecturerCode = lecturer.getLecturerCode();
        } else if (user instanceof Admin admin) {
            lecturerCode = admin.getAdminCode();
        }

        return new AdminUserManagementResponse(user.getID(), user.getUsername(), user.getEmail(),
                user.getRole(), studentCode, lecturerCode, jiraAccountId, githubUsername);
    }
}

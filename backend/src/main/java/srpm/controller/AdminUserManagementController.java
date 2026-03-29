package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.AdminUserManagementRequest;
import srpm.dto.response.AdminUserManagementResponse;
import srpm.dto.response.ApiResponse;
import srpm.model.User;
import srpm.service.AdminUserManagementService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173")
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
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách người dùng thành công", users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable String id) {
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

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody AdminUserManagementRequest request) {
        try {
            User user = adminUserManagementService.createManagedUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tạo người dùng thành công", toResponse(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable String id, @RequestBody AdminUserManagementRequest request) {
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
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String id) {
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
        return new AdminUserManagementResponse(user.getID(), user.getUsername(), user.getEmail(), user.getRole(), null);
    }
}


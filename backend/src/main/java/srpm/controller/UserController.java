package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.UpdateUserRequest;
import srpm.dto.request.UpdateLecturerRequest;
import srpm.dto.request.UpdateStudentRequest;
import srpm.dto.response.ApiResponse;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;
import srpm.service.impl.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Lấy thông tin user theo ID
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        try {
            var userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin user thành công", userOpt.get()));
            } else {
                return ResponseEntity.status(404).body(new ApiResponse(false, "User không tồn tại", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    // Cập nhật thông tin user chung (username, email, password)
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {
        try {
            if (request.getUsername() == null && request.getEmail() == null && request.getPassword() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Phải cung cấp ít nhất một thông tin để cập nhật", null));
            }

            User updatedUser = userService.updateUserInfo(userId, request);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin user thành công", updatedUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    // Cập nhật thông tin Lecturer (username, email, password, lecturerId)
    @PutMapping("/{userId}/lecturer")
    public ResponseEntity<ApiResponse> updateLecturerInfo(
            @PathVariable Long userId,
            @RequestBody UpdateLecturerRequest request) {
        try {
            if (request.getUsername() == null && request.getEmail() == null &&
                request.getPassword() == null && request.getLecturerId() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Phải cung cấp ít nhất một thông tin để cập nhật", null));
            }

            Lecturer updatedLecturer = userService.updateLecturerInfo(userId, request);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin Lecturer thành công", updatedLecturer));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    // Cập nhật thông tin Student (username, email, password, studentId, jiraAccountId, githubUsername)
    @PutMapping("/{userId}/student")
    public ResponseEntity<ApiResponse> updateStudentInfo(
            @PathVariable Long userId,
            @RequestBody UpdateStudentRequest request) {
        try {
            if (request.getUsername() == null && request.getEmail() == null &&
                request.getPassword() == null && request.getStudentId() == null &&
                request.getJiraAccountId() == null && request.getGithubUsername() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Phải cung cấp ít nhất một thông tin để cập nhật", null));
            }

            Student updatedStudent = userService.updateStudentInfo(userId, request);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin Student thành công", updatedStudent));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }
}


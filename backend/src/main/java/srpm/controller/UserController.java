package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.UpdateUserRequest;
import srpm.dto.request.UpdateLecturerRequest;
import srpm.dto.request.UpdateStudentRequest;
import srpm.dto.request.ChangePasswordRequest;
import srpm.dto.response.ApiResponse;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;
import srpm.service.IAuthorizationService;
import srpm.service.impl.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;
    private final IAuthorizationService authorizationService;

    @Autowired
    public UserController(UserService userService, IAuthorizationService authorizationService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        try {
            User currentUser = authorizationService.getCurrentUser()
                    .orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Người dùng chưa xác thực", null));
            }
            if (!currentUser.getID().equals(userId) && !currentUser.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Bạn không có quyền xem thông tin của người dùng khác", null));
            }

            var userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Object responseData = userService.toUserResponse(user);
                return ResponseEntity.ok(new ApiResponse(true, "Lấy thông tin user thành công", responseData));
            } else {
                return ResponseEntity.status(404).body(new ApiResponse(false, "User không tồn tại", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUserInfo(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        try {
            User currentUser = authorizationService.getCurrentUser()
                    .orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Người dùng chưa xác thực", null));
            }
            if (!currentUser.getID().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Bạn không có quyền cập nhật thông tin của người dùng khác", null));
            }

            User updatedUser = userService.updateUserInfo(userId, request);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin user thành công", userService.toUserResponse(updatedUser)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<ApiResponse> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        try {
            User currentUser = authorizationService.getCurrentUser()
                    .orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Người dùng chưa xác thực", null));
            }
            if (!currentUser.getID().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Bạn không có quyền đổi mật khẩu của người dùng khác", null));
            }

            userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse(true, "Đổi mật khẩu thành công", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}/lecturer")
    public ResponseEntity<ApiResponse> updateLecturerInfo(@PathVariable Long userId, @RequestBody UpdateLecturerRequest request) {
        try {
            User currentUser = authorizationService.getCurrentUser()
                    .orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Người dùng chưa xác thực", null));
            }
            if (!currentUser.getID().equals(userId) && !currentUser.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Bạn không có quyền cập nhật thông tin của người dùng khác", null));
            }

            Lecturer updatedLecturer = userService.updateLecturerInfo(userId, request);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin Lecturer thành công", userService.toUserResponse(updatedLecturer)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}/student")
    public ResponseEntity<ApiResponse> updateStudentInfo(@PathVariable Long userId, @RequestBody UpdateStudentRequest request) {
        try {
            User currentUser = authorizationService.getCurrentUser()
                    .orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Người dùng chưa xác thực", null));
            }
            if (!currentUser.getID().equals(userId) && !currentUser.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Bạn không có quyền cập nhật thông tin của người dùng khác", null));
            }

            Student updatedStudent = userService.updateStudentInfo(userId, request);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin Student thành công", userService.toUserResponse(updatedStudent)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }
}

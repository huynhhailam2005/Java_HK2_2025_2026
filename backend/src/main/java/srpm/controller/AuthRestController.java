package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.LoginRequest;
import srpm.dto.request.RegisterRequest;
import srpm.dto.response.LoginResponse;
import srpm.model.UserFactory;
import srpm.service.UserService;
import srpm.model.User;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthRestController {

    private final UserService userService;

    @Autowired
    public AuthRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (userOpt.isPresent()) {
            return ResponseEntity.ok(new LoginResponse(true, "Đăng nhập thành công", userOpt.get()));
        } else {
            return ResponseEntity.status(401).body(new LoginResponse(false, "Sai tài khoản hoặc mật khẩu", null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest regRequest) {
        try {
            if (regRequest.getUsername() == null || regRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new LoginResponse(false, "Username không được để trống", null));
            }
            if (regRequest.getEmail() == null || regRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new LoginResponse(false, "Email không được để trống", null));
            }
            if ("ADMIN".equalsIgnoreCase(regRequest.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new LoginResponse(false, "Không được phép đăng ký tài khoản Admin", null));
            }
            User user = UserFactory.createUser(regRequest.getRole());

            user.setUsername(regRequest.getUsername());
            user.setPassword(regRequest.getPassword());
            user.setEmail(regRequest.getEmail());

            userService.createUser(user);

            return ResponseEntity.ok(new LoginResponse(true, "Đăng ký tài khoản thành công!", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }
}
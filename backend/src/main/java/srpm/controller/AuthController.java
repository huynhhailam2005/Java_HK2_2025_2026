package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.LoginRequest;
import srpm.dto.request.RegisterRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.AuthResponse;
import srpm.model.*;
import srpm.security.JwtService;
import srpm.service.UserService;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("[DEBUG] Login attempt: username=" + loginRequest.getUsername());
            Optional<User> userOpt = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("[DEBUG] User found: " + user.getUsername() + ", role=" + user.getRole());
                String token = jwtService.generateToken(user);
                AuthResponse data = new AuthResponse(
                        token,
                        new AuthResponse.UserPayload(user.getID(), user.getUsername(), user.getEmail(), user.getRole())
                );

                return ResponseEntity.ok(new ApiResponse(true, "Đăng nhập thành công", data));
            } else {
                System.out.println("[DEBUG] User not found or password incorrect");
                return ResponseEntity.status(401).body(new ApiResponse(false, "Sai tài khoản hoặc mật khẩu", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest regRequest) {
        try {
            if (regRequest.getUsername() == null || regRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Username không được để trống", null));
            }
            if (regRequest.getEmail() == null || regRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Email không được để trống", null));
            }
            if (regRequest.getPassword() == null || regRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Password không được để trống", null));
            }

            User user;
            String businessCode = UUID.randomUUID().toString().substring(0, 20);

            if ("ADMIN".equalsIgnoreCase(regRequest.getRole())) {
                user = new Admin(regRequest.getUsername(), regRequest.getPassword(),
                        regRequest.getEmail(), UserRole.ADMIN, businessCode);
            } else if ("LECTURER".equalsIgnoreCase(regRequest.getRole())) {
                user = new Lecturer(regRequest.getUsername(), regRequest.getPassword(),
                        regRequest.getEmail(), UserRole.LECTURER, businessCode);
            } else {
                user = new Student(regRequest.getUsername(), regRequest.getPassword(),
                        regRequest.getEmail(), UserRole.STUDENT, businessCode);
            }

            userService.createUser(user);

            return ResponseEntity.ok(new ApiResponse(true, "Đăng ký tài khoản thành công!", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }
}
package srpm.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.LoginRequest;
import srpm.dto.request.RegisterRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.AuthResponse;
import srpm.exception.ValidationException;
import srpm.model.*;
import srpm.security.JwtService;
import srpm.service.impl.UserService;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.debug("Login attempt: username={}", loginRequest.getUsername());

        Optional<User> userOpt = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("User login successful: username={}, role={}", user.getUsername(), user.getRole());
            String token = jwtService.generateToken(user);
            AuthResponse data = new AuthResponse(
                    token,
                    new AuthResponse.UserPayload(user.getID(), user.getUsername(), user.getEmail(), user.getRole())
            );
            return ResponseEntity.ok(new ApiResponse(true, "Đăng nhập thành công", data));
        } else {
            logger.warn("Login failed: username={}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Sai tài khoản hoặc mật khẩu", null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest regRequest) {
        logger.debug("Register attempt: username={}, email={}", regRequest.getUsername(), regRequest.getEmail());

        try {
            String generatedCode = UUID.randomUUID().toString().substring(0, 20);

            // Sử dụng UserFactory để tạo user object
            User user = UserFactory.createUser(regRequest.getRole());
            if (user == null) {
                throw new ValidationException("Role không hợp lệ: " + regRequest.getRole());
            }

            user.setUsername(regRequest.getUsername());
            user.setPassword(regRequest.getPassword());
            user.setEmail(regRequest.getEmail());

            // Set role-specific code
            if (user instanceof Admin) {
                ((Admin) user).setAdminCode(generatedCode);
            } else if (user instanceof Lecturer) {
                ((Lecturer) user).setLecturerCode(generatedCode);
            } else if (user instanceof Student) {
                ((Student) user).setStudentCode(generatedCode);
            }

            userService.createUser(user);
            logger.info("User registered successfully: username={}, role={}", user.getUsername(), user.getRole());

            return ResponseEntity.ok(new ApiResponse(true, "Đăng ký tài khoản thành công!", null));

        } catch (ValidationException e) {
            logger.warn("Validation error during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống", null));
        }
    }
}
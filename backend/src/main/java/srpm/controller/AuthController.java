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
import srpm.model.User;
import srpm.security.JwtService;
import srpm.service.impl.UserService;
import java.util.Optional;

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
            userService.registerUser(regRequest);
            logger.info("User registered successfully: username={}", regRequest.getUsername());
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
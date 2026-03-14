package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.entity.User;
import srpm.service.UserService;
import srpm.dto.LoginRequest;
import srpm.dto.LoginResponse;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Endpoint đăng nhập
     * POST /api/auth/login
     * Use case: Login -> Verify password -> Display login error (nếu thất bại)
     * 
     * @param loginRequest - chứa username và password
     * @return thông tin người dùng hoặc lỗi
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // Validate input
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            LoginResponse response = new LoginResponse(false, "Vui lòng nhập đầy đủ username và password", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // Debug
        System.out.println("Login request received: " + loginRequest.getUsername() + " / " + loginRequest.getPassword());
        
        // Gọi service để kiểm tra đăng nhập (Verify password)
        Optional<User> user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        
        if (user.isPresent()) {
            // Đăng nhập thành công
            LoginResponse response = new LoginResponse(true, "Đăng nhập thành công", user.get());
            return ResponseEntity.ok(response);
        } else {
            // Đăng nhập thất bại - Display login error
            LoginResponse response = new LoginResponse(false, "Username hoặc password không đúng", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * Debug endpoint
     */
    @PostMapping("/test-login")
    public ResponseEntity<String> testLogin(@RequestBody String body) {
        return ResponseEntity.ok("Received: " + body);
    }
    
    /**
     * Lấy thông tin người dùng theo ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy người dùng"));
    }
    
    /**
     * Tạo người dùng mới
     */
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody User user) {
        try {
            // Kiểm tra username đã tồn tại chưa
            if (userService.getUserByUsername(user.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Username đã tồn tại"));
            }
            
            User newUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Tạo tài khoản thành công", newUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Lỗi tạo tài khoản: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method để tạo response thành công
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
    
    /**
     * Helper method để tạo response lỗi
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}


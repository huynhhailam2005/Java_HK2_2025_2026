package srpm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthRestController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        Map<String, Object> response = new HashMap<>();

        // Check dữ liệu rỗng
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()
                || loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {

            response.put("status", "error");
            response.put("message", "Username hoặc password không được để trống");

            return ResponseEntity.badRequest().body(response);
        }

        if ("admin".equals(loginRequest.getUsername()) && "123456".equals(loginRequest.getPassword())) {
            response.put("status", "success");
            response.put("message", "Đăng nhập thành công!");
            response.put("token", "fake-jwt-token-123456");

            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Sai tên đăng nhập hoặc mật khẩu.");

            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {

        Map<String, Object> response = new HashMap<>();

        // Check dữ liệu rỗng
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()
                || registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()
                || registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {

            response.put("status", "error");
            response.put("message", "Username, password và email không được để trống");

            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "success");
        response.put("message", "Đăng ký thành công tài khoản: " + registerRequest.getUsername());

        return ResponseEntity.ok(response);
    }
}

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class RegisterRequest {
    private String username;
    private String password;
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
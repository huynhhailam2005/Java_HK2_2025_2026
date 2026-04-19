package srpm.dto.request;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Role không được để trống")
    @Pattern(regexp = "ADMIN|LECTURER|STUDENT", message = "Role phải là ADMIN, LECTURER hoặc STUDENT")
    private String role;

    private String businessId;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public RegisterRequest(String username, String password, String email, String role, String businessId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.businessId = businessId;
    }

    public String getUsername() { return this.username; }
    public String getPassword() {return this.password; }
    public String getEmail() { return this.email; }
    public String getRole() { return this.role; }
    public String getBusinessId() { return this.businessId; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
}
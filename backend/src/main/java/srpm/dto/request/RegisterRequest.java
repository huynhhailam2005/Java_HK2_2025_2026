package srpm.dto.request;

public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String role;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String email, String role, String studentId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public String getUsername() { return this.username; }
    public String getPassword() {return this.password; }
    public String getEmail() { return this.email; }
    public String getRole() { return this.role; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
}
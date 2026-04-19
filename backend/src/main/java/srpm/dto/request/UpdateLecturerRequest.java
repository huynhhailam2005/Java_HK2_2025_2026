package srpm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateLecturerRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải tối thiểu 6 ký tự")
    private String password;

    private String lecturerId;

    public UpdateLecturerRequest() {}

    public UpdateLecturerRequest(String username, String email, String password, String lecturerId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.lecturerId = lecturerId;
    }

    public String getUsername() { return this.username; }
    public String getEmail() { return this.email; }
    public String getPassword() { return this.password; }
    public String getLecturerId() { return this.lecturerId; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
}


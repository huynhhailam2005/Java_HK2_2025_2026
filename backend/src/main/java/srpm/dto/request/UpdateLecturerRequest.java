package srpm.dto.request;

public class UpdateLecturerRequest {
    private String username;
    private String email;
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


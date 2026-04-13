package srpm.dto.request;

public class LecturerManagementRequest {
    private String username;
    private String password;
    private String email;
    private String lecturerCode;

    public LecturerManagementRequest() {}

    public LecturerManagementRequest(String username, String password, String email, String lecturerCode) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.lecturerCode = lecturerCode;
    }

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

    public String getLecturerCode() {
        return lecturerCode;
    }

    public void setLecturerCode(String lecturerCode) {
        this.lecturerCode = lecturerCode;
    }
}


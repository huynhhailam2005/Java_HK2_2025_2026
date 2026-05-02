package srpm.dto.request;

public class UpdateUserRequest {
    private String username;
    private String email;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() { return this.username; }
    public String getEmail() { return this.email; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
}


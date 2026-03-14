package srpm.model;

public abstract class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private Role role;

    public User() {}

    public User(String id, String username, String password, String email, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public String getID() { return this.id; }
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public String getEmail() { return this.email; }
    public Role getRole() {return  this.role; }

    public void setID(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
}


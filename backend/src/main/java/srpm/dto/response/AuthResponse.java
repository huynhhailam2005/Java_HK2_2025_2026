package srpm.dto.response;

import srpm.model.UserRole;

public class AuthResponse {
    private String token;
    private UserPayload user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserPayload user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserPayload getUser() {
        return user;
    }

    public void setUser(UserPayload user) {
        this.user = user;
    }

    public static class UserPayload {
        private Long id;
        private String username;
        private String email;
        private UserRole userRole;

        public UserPayload() {
        }

        public UserPayload(Long id, String username, String email, UserRole userRole) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.userRole = userRole;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public UserRole getRole() {
            return userRole;
        }

        public void setRole(UserRole userRole) {
            this.userRole = userRole;
        }
    }
}


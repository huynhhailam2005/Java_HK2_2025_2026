package srpm.model;

public class UserFactory {
    public static User createUser(String roleStr) {
        if (roleStr == null) { return null; }

        UserRole userRole = UserRole.fromValue(roleStr);

        User user = switch (userRole) {
            case ADMIN -> new Admin();
            case LECTURER -> new Lecturer();
            case STUDENT -> new Student();
            default -> throw new IllegalArgumentException("Unknown role: " + userRole);
        };

        user.setRole(userRole);

        return user;
    }
}
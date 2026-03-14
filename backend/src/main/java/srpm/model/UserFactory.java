package srpm.model;

public class UserFactory {
    public static User createUser(String roleStr) {
        if (roleStr == null) { return null; }

        Role role = Role.valueOf(roleStr.toUpperCase());

        return switch (role) {
            case ADMIN -> new Admin();
            case LECTURER -> new Lecturer();
            case STUDENT -> new Student();
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}

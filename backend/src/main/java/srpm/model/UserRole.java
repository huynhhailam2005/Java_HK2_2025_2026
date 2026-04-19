package srpm.model;

public enum UserRole {
    ADMIN("ADMIN"),
    LECTURER("LECTURER"),
    STUDENT("STUDENT");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid UserRole: " + value);
    }
}

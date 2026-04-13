package srpm.model;

public enum GroupMemberRole {
    TEAM_MEMBER("member"),
    TEAM_LEADER("leader");

    private final String value;

    GroupMemberRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GroupMemberRole fromValue(String value) {
        for (GroupMemberRole role : GroupMemberRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid GroupMemberRole: " + value);
    }
}

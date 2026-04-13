package srpm.model;

public enum IssueType {
    TASK("TASK"),
    BUG("BUG"),
    STORY("STORY"),
    SUB_TASK("SUB_TASK"),
    EPIC("EPIC");

    private final String value;

    IssueType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IssueType fromValue(String value) {
        for (IssueType type : IssueType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid IssueType: " + value);
    }
}


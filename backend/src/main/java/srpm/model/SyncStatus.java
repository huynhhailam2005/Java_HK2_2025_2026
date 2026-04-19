package srpm.model;

public enum SyncStatus {
    PENDING("PENDING"),
    SYNCED("SYNCED"),
    ERROR("ERROR");

    private final String value;

    SyncStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SyncStatus fromValue(String value) {
        for (SyncStatus status : SyncStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid SyncStatus: " + value);
    }
}


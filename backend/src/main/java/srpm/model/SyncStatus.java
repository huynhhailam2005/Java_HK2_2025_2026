package srpm.model;

/**
 * Trạng thái đồng bộ của Issue giữa SRPM và Jira
 */
public enum SyncStatus {
    PENDING,  // Đang chờ đồng bộ
    SYNCED,   // Đã đồng bộ thành công
    ERROR     // Lỗi đồng bộ
}


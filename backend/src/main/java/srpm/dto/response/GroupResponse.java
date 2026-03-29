package srpm.dto.response;

public class GroupResponse {
    private boolean success;
    private String message;
    private GroupDto data;

    public GroupResponse() {}

    public GroupResponse(boolean success, String message, GroupDto data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public GroupDto getData() { return data; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(GroupDto data) { this.data = data; }
}


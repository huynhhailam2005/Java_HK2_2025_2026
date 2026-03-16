package srpm.dto.response;

public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    public Object getData() {
        return data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setMessage(String message) { this.message = message; }
    public void setData(Object data) {
        this.data = data;
    }
}


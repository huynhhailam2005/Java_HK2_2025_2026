package srpm.dto.response;

public class RegisterResponse {
    private boolean success;
    private String message;
    private Object data;

    public RegisterResponse() {}

    public RegisterResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return this.success; }
    public String getMessage() { return this.message; }
    public Object getData() { return this.data; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(Object data) { this.data = data; }
}
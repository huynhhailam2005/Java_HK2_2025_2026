package srpm.dto.response;

import srpm.model.Topic;

public class TopicResponse {
    private boolean success;
    private String message;
    private Topic data;

    public TopicResponse() {}

    public TopicResponse(boolean success, String message, Topic data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Topic getData() { return data; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(Topic data) { this.data = data; }
}

package srpm.dto.request;

public class TaskCommentRequest {
    private String content;
    private String authorId;  // user id của người comment

    public TaskCommentRequest() {}

    public TaskCommentRequest(String content, String authorId) {
        this.content = content;
        this.authorId = authorId;
    }

    public String getContent() { return content; }
    public String getAuthorId() { return authorId; }

    public void setContent(String content) { this.content = content; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
}

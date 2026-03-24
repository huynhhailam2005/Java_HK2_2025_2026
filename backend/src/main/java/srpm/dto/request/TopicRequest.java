package srpm.dto.request;

public class TopicRequest {
    private String title;
    private String description;
    private String lecturerId;
    private String studentId;  // optional

    public TopicRequest() {}

    public TopicRequest(String title, String description, String lecturerId, String studentId) {
        this.title = title;
        this.description = description;
        this.lecturerId = lecturerId;
        this.studentId = studentId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLecturerId() { return lecturerId; }
    public String getStudentId() { return studentId; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}

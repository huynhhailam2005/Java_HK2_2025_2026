package srpm.dto.request;

import java.util.List;

public class GroupRequest {
    private String title;
    private String description;
    private String lecturerId;
    private List<String> studentIds;  // optional

    public GroupRequest() {}

    public GroupRequest(String title, String description, String lecturerId, List<String> studentIds) {
        this.title = title;
        this.description = description;
        this.lecturerId = lecturerId;
        this.studentIds = studentIds;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLecturerId() { return lecturerId; }
    public List<String> getStudentIds() { return studentIds; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public void setStudentIds(List<String> studentIds) { this.studentIds = studentIds; }
}


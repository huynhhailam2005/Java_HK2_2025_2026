package srpm.model;

public class Submission {

    private Long id;
    private String fileUrl;
    private String note;
    private Long studentId;
    private Long projectId;

    public Submission() {
    }

    public Submission(Long id, String fileUrl, String note, Long studentId, Long projectId) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.note = note;
        this.studentId = studentId;
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) { // FIX thiếu setter
        this.id = id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
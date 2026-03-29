package srpm.dto.response;

import srpm.model.Group;
import srpm.model.Student;

import java.util.List;

public class GroupDto {
    private String id;
    private String title;
    private String description;
    private String lecturerId;
    private String lecturerUsername;
    private List<String> studentIds;

    public GroupDto() {
    }

    public GroupDto(String id, String title, String description, String lecturerId, String lecturerUsername, List<String> studentIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lecturerId = lecturerId;
        this.lecturerUsername = lecturerUsername;
        this.studentIds = studentIds;
    }

    public static GroupDto fromEntity(Group group) {
        List<String> ids = group.getStudents() == null
                ? List.of()
                : group.getStudents().stream().map(Student::getID).toList();

        String resolvedLecturerId = group.getLecturer() == null ? null : group.getLecturer().getID();
        String resolvedLecturerUsername = group.getLecturer() == null ? null : group.getLecturer().getUsername();

        return new GroupDto(
                group.getId(),
                group.getTitle(),
                group.getDescription(),
                resolvedLecturerId,
                resolvedLecturerUsername,
                ids
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getLecturerUsername() {
        return lecturerUsername;
    }

    public void setLecturerUsername(String lecturerUsername) {
        this.lecturerUsername = lecturerUsername;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }
}


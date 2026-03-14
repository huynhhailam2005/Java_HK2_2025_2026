package srpm.model;

public class Student extends User {
    private String studentId;

    public Student(){
        super();
    }

    public Student(String id, String username, String password, String email, Role role, String studentId) {
        super(id, username, password, email, role);
        this.studentId = studentId;
    }

    public String getStudentId() { return this.studentId; }

    public void setStudentId(String studentId) { this.studentId = studentId; }
}

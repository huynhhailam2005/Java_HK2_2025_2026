package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("STUDENT")
public class Student extends User {

    @Column(name = "student_id", length = 20)
    private String studentId;

    public Student() {
        super();
    }

    public Student(String id, String username, String password, String email, Role role, String studentId) {
        super(id, username, password, email, role);
        this.studentId = studentId;
    }

    public String getStudentId() { return this.studentId; }

    public void setStudentId(String studentId) { this.studentId = studentId; }
}


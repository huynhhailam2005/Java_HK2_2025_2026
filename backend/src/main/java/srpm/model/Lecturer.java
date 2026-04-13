package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "lecturers")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("LECTURER")
public class Lecturer extends User {

    @Column(name = "lecturer_code", length = 20, nullable = false, unique = true)
    private String lecturerCode;

    public Lecturer() {
        super();
    }

    public Lecturer(String username, String password, String email, UserRole userRole, String lecturerCode) {
        super(username, password, email, userRole);
        this.lecturerCode = lecturerCode;
    }

    public Long getId() {
        return this.getID();
    }

    public String getLecturerCode() { return this.lecturerCode; }

    public void setLecturerCode(String lecturerCode) { this.lecturerCode = lecturerCode; }
}


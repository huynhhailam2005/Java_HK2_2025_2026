package srpm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    @JsonIgnoreProperties({"password", "email"})
    private Lecturer lecturer;

    @ManyToMany
    @JoinTable(
            name = "group_students",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnoreProperties({"password", "email"})
    private Set<Student> students = new HashSet<>();

    public Group() {}

    public Group(String id, String title, String description,
                 Lecturer lecturer, Set<Student> students) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lecturer = lecturer;
        this.students = students == null ? new HashSet<>() : students;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Lecturer getLecturer() { return lecturer; }
    public Set<Student> getStudents() { return students; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }
    public void setStudents(Set<Student> students) { this.students = students == null ? new HashSet<>() : students; }
}


package srpm.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "lecturers")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("LECTURER")
public class Lecturer extends User {
}


package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.Student;

public interface StudentRepository extends JpaRepository<Student, String> {

    boolean existsByStudentId(String studentId);

    boolean existsByStudentIdAndIdNot(String studentId, String id);
}


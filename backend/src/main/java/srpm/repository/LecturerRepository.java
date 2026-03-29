package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.Lecturer;

public interface LecturerRepository extends JpaRepository<Lecturer, String> {
}


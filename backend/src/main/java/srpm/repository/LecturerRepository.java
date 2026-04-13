package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import srpm.model.Lecturer;
import java.util.Optional;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    @Query("SELECT l FROM Lecturer l WHERE l.id = :userId")
    Optional<Lecturer> findByUserId(@Param("userId") Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByLecturerCode(String lecturerCode);

    boolean existsByLecturerCodeAndIdNot(String lecturerCode, Long id);
}

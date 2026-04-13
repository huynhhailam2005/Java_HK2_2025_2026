package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import srpm.model.Student;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentCode(String studentCode);

    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT s FROM Student s WHERE s.id = :userId")
    Optional<Student> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Student s WHERE s.jiraAccountId = :jiraAccountId")
    Optional<Student> findByJiraAccountId(@Param("jiraAccountId") String jiraAccountId);

    @Query("SELECT s FROM Student s WHERE s.username = :username")
    Optional<Student> findByUsername(@Param("username") String username);

    @Query("SELECT s FROM Student s WHERE s.email = :email")
    Optional<Student> findByEmail(@Param("email") String email);
}

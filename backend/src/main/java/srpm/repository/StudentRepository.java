package srpm.repository;

import srpm.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository wrapper contract for {@link Student}.
 */
public interface StudentRepository {

	Optional<Student> findById(Long id);

	Optional<Student> findByUserId(Long userId);

	Optional<Student> findByJiraAccountId(String jiraAccountId);

	Optional<Student> findByUsername(String username);

	Optional<Student> findByEmail(String email);

	boolean existsByStudentCode(String studentCode);

	boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsernameAndIdNot(String username, Long id);

	boolean existsByEmailAndIdNot(String email, Long id);

	Student save(Student student);

	List<Student> saveAll(List<Student> students);
}

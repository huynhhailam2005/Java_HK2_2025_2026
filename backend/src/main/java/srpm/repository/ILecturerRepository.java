package srpm.repository;

import srpm.model.Lecturer;

import java.util.Optional;

public interface ILecturerRepository {
	Optional<Lecturer> findById(Long id);

	Optional<Lecturer> findByUserId(Long userId);

	Lecturer save(Lecturer lecturer);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsernameAndIdNot(String username, Long id);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByLecturerCode(String lecturerCode);

	boolean existsByLecturerCodeAndIdNot(String lecturerCode, Long id);
}

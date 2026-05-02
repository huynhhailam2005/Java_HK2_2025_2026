package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.LecturerDao;
import srpm.model.Lecturer;
import srpm.repository.ILecturerRepository;

import java.util.Optional;

@Repository
public class LecturerRepository implements ILecturerRepository {

	private final LecturerDao lecturerDao;

	public LecturerRepository(LecturerDao lecturerDao) {
		this.lecturerDao = lecturerDao;
	}

	@Override
	public Optional<Lecturer> findById(Long id) {
		return lecturerDao.findById(id);
	}

	@Override
	public Optional<Lecturer> findByUserId(Long userId) {
		return lecturerDao.findByUserId(userId);
	}

	@Override
	public Lecturer save(Lecturer lecturer) {
		return lecturerDao.save(lecturer);
	}

	@Override
	public boolean existsByUsername(String username) {
		return lecturerDao.existsByUsername(username);
	}

	@Override
	public boolean existsByEmail(String email) {
		return lecturerDao.existsByEmail(email);
	}

	@Override
	public boolean existsByUsernameAndIdNot(String username, Long id) {
		return lecturerDao.existsByUsernameAndIdNot(username, id);
	}

	@Override
	public boolean existsByEmailAndIdNot(String email, Long id) {
		return lecturerDao.existsByEmailAndIdNot(email, id);
	}

	@Override
	public boolean existsByLecturerCode(String lecturerCode) {
		return lecturerDao.existsByLecturerCode(lecturerCode);
	}

	@Override
	public boolean existsByLecturerCodeAndIdNot(String lecturerCode, Long id) {
		return lecturerDao.existsByLecturerCodeAndIdNot(lecturerCode, id);
	}
}


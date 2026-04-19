package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.StudentDao;
import srpm.model.Student;
import srpm.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

	private final StudentDao studentDao;

	public StudentRepositoryImpl(StudentDao studentDao) {
		this.studentDao = studentDao;
	}

	@Override
	public Optional<Student> findById(Long id) {
		return studentDao.findById(id);
	}

	@Override
	public Optional<Student> findByUserId(Long userId) {
		return studentDao.findByUserId(userId);
	}

	@Override
	public Optional<Student> findByJiraAccountId(String jiraAccountId) {
		return studentDao.findByJiraAccountId(jiraAccountId);
	}

	@Override
	public Optional<Student> findByUsername(String username) {
		return studentDao.findByUsername(username);
	}

	@Override
	public Optional<Student> findByEmail(String email) {
		return studentDao.findByEmail(email);
	}

	@Override
	public boolean existsByStudentCode(String studentCode) {
		return studentDao.existsByStudentCode(studentCode);
	}

	@Override
	public boolean existsByStudentCodeAndIdNot(String studentCode, Long id) {
		return studentDao.existsByStudentCodeAndIdNot(studentCode, id);
	}

	@Override
	public boolean existsByUsername(String username) {
		return studentDao.existsByUsername(username);
	}

	@Override
	public boolean existsByEmail(String email) {
		return studentDao.existsByEmail(email);
	}

	@Override
	public boolean existsByUsernameAndIdNot(String username, Long id) {
		return studentDao.existsByUsernameAndIdNot(username, id);
	}

	@Override
	public boolean existsByEmailAndIdNot(String email, Long id) {
		return studentDao.existsByEmailAndIdNot(email, id);
	}

	@Override
	public Student save(Student student) {
		return studentDao.save(student);
	}

	@Override
	public List<Student> saveAll(List<Student> students) {
		return studentDao.saveAll(students);
	}
}


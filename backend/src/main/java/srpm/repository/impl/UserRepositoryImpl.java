package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.UserDao;
import srpm.model.User;
import srpm.model.UserRole;
import srpm.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

	private final UserDao userDao;

	public UserRepositoryImpl(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public Optional<User> findById(Long id) {
		return userDao.findById(id);
	}

	@Override
	public Optional<User> findByUsernameOrEmail(String username, String email) {
		return userDao.findByUsernameOrEmail(username, email);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userDao.findByEmail(email);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return userDao.findByUsername(username);
	}

	@Override
	public boolean existsByUsername(String username) {
		return userDao.existsByUsername(username);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userDao.existsByEmail(email);
	}

	@Override
	public boolean existsByUsernameAndIdNot(String username, Long id) {
		return userDao.existsByUsernameAndIdNot(username, id);
	}

	@Override
	public boolean existsByEmailAndIdNot(String email, Long id) {
		return userDao.existsByEmailAndIdNot(email, id);
	}

	@Override
	public List<User> findAllByUserRoleOrderByUsernameAsc(UserRole userRole) {
		return userDao.findAllByUserRoleOrderByUsernameAsc(userRole);
	}

	@Override
	public List<User> findAllByUserRoleInOrderByUsernameAsc(List<UserRole> userRoles) {
		return userDao.findAllByUserRoleInOrderByUsernameAsc(userRoles);
	}

	@Override
	public Optional<User> findByIdAndUserRoleIn(Long id, List<UserRole> userRoles) {
		return userDao.findByIdAndUserRoleIn(id, userRoles);
	}

	@Override
	public User save(User user) {
		return userDao.save(user);
	}

	@Override
	public void deleteById(Long id) {
		userDao.deleteById(id);
	}
}


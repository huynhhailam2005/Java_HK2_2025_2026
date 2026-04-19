package srpm.repository;

import srpm.model.User;
import srpm.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository wrapper contract for {@link User}.
 * <p>
 * This layer intentionally does NOT depend on Spring Data; it wraps the DAO layer
 * and is the default dependency for services.
 */
public interface UserRepository {

	Optional<User> findById(Long id);

	Optional<User> findByUsernameOrEmail(String username, String email);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsernameAndIdNot(String username, Long id);

	boolean existsByEmailAndIdNot(String email, Long id);

	List<User> findAllByUserRoleOrderByUsernameAsc(UserRole userRole);

	List<User> findAllByUserRoleInOrderByUsernameAsc(List<UserRole> userRoles);

	Optional<User> findByIdAndUserRoleIn(Long id, List<UserRole> userRoles);

	User save(User user);

	void deleteById(Long id);
}

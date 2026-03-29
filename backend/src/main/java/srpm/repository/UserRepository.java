package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.Role;
import srpm.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, String id);

    boolean existsByEmailAndIdNot(String email, String id);


    List<User> findAllByRoleOrderByUsernameAsc(Role role);

    List<User> findAllByRoleInOrderByUsernameAsc(List<Role> roles);

    Optional<User> findByIdAndRoleIn(String id, List<Role> roles);
}

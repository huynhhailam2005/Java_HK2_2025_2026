package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import srpm.model.UserRole;
import srpm.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<User> findAllByUserRoleOrderByUsernameAsc(UserRole userRole);

    List<User> findAllByUserRoleInOrderByUsernameAsc(List<UserRole> userRoles);

    Optional<User> findByIdAndUserRoleIn(Long id, List<UserRole> userRoles);

    Optional<User> findByUsername(String username);
}

package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import srpm.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Tìm kiếm người dùng theo username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Kiểm tra xem username đã tồn tại chưa
     */
    boolean existsByUsername(String username);
}


package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.Group;

public interface GroupRepository extends JpaRepository<Group, String> {
}



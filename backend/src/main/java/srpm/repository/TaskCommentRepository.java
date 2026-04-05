package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.TaskComment;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}

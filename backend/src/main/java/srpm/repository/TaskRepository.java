package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.Task;
import srpm.model.TaskStatus;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByGroupId(String groupId);

    List<Task> findByGroupIdAndStatus(String groupId, TaskStatus status);

    List<Task> findByAssigneeId(String studentId);
}

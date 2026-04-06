package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.TaskCommentRequest;
import srpm.dto.response.TaskCommentDto;
import srpm.model.Task;
import srpm.model.TaskComment;
import srpm.model.User;
import srpm.repository.TaskCommentRepository;
import srpm.repository.TaskRepository;
import srpm.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskCommentService(TaskCommentRepository taskCommentRepository,
                              TaskRepository taskRepository,
                              UserRepository userRepository) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /** Thêm comment vào task */
    @Transactional
    public TaskCommentDto addComment(Long taskId, TaskCommentRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task: " + taskId));
        User author = userRepository.findById(req.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + req.getAuthorId()));

        TaskComment comment = new TaskComment(req.getContent(), task, author);
        return TaskCommentDto.fromEntity(taskCommentRepository.save(comment));
    }

    /** Lấy tất cả comment của task */
    public List<TaskCommentDto> getCommentsByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Không tìm thấy task: " + taskId);
        }
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream().map(TaskCommentDto::fromEntity).collect(Collectors.toList());
    }
}

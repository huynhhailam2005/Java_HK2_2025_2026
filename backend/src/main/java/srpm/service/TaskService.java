package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.TaskRequest;
import srpm.dto.response.TaskDto;
import srpm.model.*;
import srpm.repository.GroupRepository;
import srpm.repository.StudentRepository;
import srpm.repository.TaskRepository;
import srpm.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       GroupRepository groupRepository,
                       StudentRepository studentRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    /** Tạo task mới trong một group */
    @Transactional
    public TaskDto createTask(String groupId, TaskRequest req) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        User createdBy = userRepository.findById(req.getCreatedById())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + req.getCreatedById()));

        Student assignee = null;
        if (req.getAssigneeId() != null && !req.getAssigneeId().isBlank()) {
            assignee = studentRepository.findById(req.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + req.getAssigneeId()));
        }

        Task task = new Task(req.getTitle(), req.getDescription(), req.getDueDate(),
                TaskStatus.TODO, group, assignee, createdBy);
        return TaskDto.fromEntity(taskRepository.save(task));
    }

    /** Lấy tất cả task của một group */
    public List<TaskDto> getTasksByGroup(String groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new RuntimeException("Không tìm thấy group: " + groupId);
        }
        return taskRepository.findByGroupId(groupId)
                .stream().map(TaskDto::fromEntity).collect(Collectors.toList());
    }

    /** Lấy task theo status trong group */
    public List<TaskDto> getTasksByGroupAndStatus(String groupId, TaskStatus status) {
        return taskRepository.findByGroupIdAndStatus(groupId, status)
                .stream().map(TaskDto::fromEntity).collect(Collectors.toList());
    }

    /** Lấy chi tiết một task */
    public TaskDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task: " + taskId));
        return TaskDto.fromEntity(task);
    }

    /** Cập nhật thông tin task */
    @Transactional
    public TaskDto updateTask(Long taskId, TaskRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task: " + taskId));

        if (req.getTitle() != null) task.setTitle(req.getTitle());
        if (req.getDescription() != null) task.setDescription(req.getDescription());
        if (req.getDueDate() != null) task.setDueDate(req.getDueDate());

        if (req.getAssigneeId() != null) {
            if (req.getAssigneeId().isBlank()) {
                task.setAssignee(null);
            } else {
                Student assignee = studentRepository.findById(req.getAssigneeId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + req.getAssigneeId()));
                task.setAssignee(assignee);
            }
        }

        return TaskDto.fromEntity(taskRepository.save(task));
    }

    /** Đổi trạng thái task */
    @Transactional
    public TaskDto updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task: " + taskId));
        task.setStatus(newStatus);
        return TaskDto.fromEntity(taskRepository.save(task));
    }

    /** Xoá task */
    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Không tìm thấy task: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }
}

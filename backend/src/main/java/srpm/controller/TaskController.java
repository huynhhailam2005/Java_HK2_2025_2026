package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.TaskCommentRequest;
import srpm.dto.request.TaskRequest;
import srpm.dto.response.ApiResponse;
import srpm.dto.response.TaskCommentDto;
import srpm.dto.response.TaskDto;
import srpm.model.TaskStatus;
import srpm.service.TaskCommentService;
import srpm.service.TaskService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskService taskService;
    private final TaskCommentService taskCommentService;

    @Autowired
    public TaskController(TaskService taskService, TaskCommentService taskCommentService) {
        this.taskService = taskService;
        this.taskCommentService = taskCommentService;
    }

    // ===================== TASK ENDPOINTS =====================

    /**
     * POST /api/groups/{groupId}/tasks
     * Tạo task mới trong một group
     */
    @PostMapping("/api/groups/{groupId}/tasks")
    public ResponseEntity<ApiResponse> createTask(
            @PathVariable String groupId,
            @RequestBody TaskRequest req) {
        try {
            TaskDto task = taskService.createTask(groupId, req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tạo task thành công", task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/groups/{groupId}/tasks
     * Lấy tất cả task của một group (có thể lọc theo status)
     */
    @GetMapping("/api/groups/{groupId}/tasks")
    public ResponseEntity<ApiResponse> getTasksByGroup(
            @PathVariable String groupId,
            @RequestParam(required = false) TaskStatus status) {
        try {
            List<TaskDto> tasks;
            if (status != null) {
                tasks = taskService.getTasksByGroupAndStatus(groupId, status);
            } else {
                tasks = taskService.getTasksByGroup(groupId);
            }
            return ResponseEntity.ok(new ApiResponse(true, "OK", tasks));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/tasks/{taskId}
     * Lấy chi tiết một task
     */
    @GetMapping("/api/tasks/{taskId}")
    public ResponseEntity<ApiResponse> getTaskById(@PathVariable Long taskId) {
        try {
            TaskDto task = taskService.getTaskById(taskId);
            return ResponseEntity.ok(new ApiResponse(true, "OK", task));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * PUT /api/tasks/{taskId}
     * Cập nhật thông tin task (title, description, dueDate, assignee)
     */
    @PutMapping("/api/tasks/{taskId}")
    public ResponseEntity<ApiResponse> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskRequest req) {
        try {
            TaskDto task = taskService.updateTask(taskId, req);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật task thành công", task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * PATCH /api/tasks/{taskId}/status
     * Đổi trạng thái task
     * Body: "IN_PROGRESS" (plain string)
     */
    @PatchMapping("/api/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody String statusStr) {
        try {
            TaskStatus newStatus = TaskStatus.valueOf(statusStr.trim().replace("\"", ""));
            TaskDto task = taskService.updateTaskStatus(taskId, newStatus);
            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật trạng thái thành công", task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Trạng thái không hợp lệ: " + statusStr, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * DELETE /api/tasks/{taskId}
     * Xoá task
     */
    @DeleteMapping("/api/tasks/{taskId}")
    public ResponseEntity<ApiResponse> deleteTask(@PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok(new ApiResponse(true, "Xoá task thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // ===================== COMMENT ENDPOINTS =====================

    /**
     * POST /api/tasks/{taskId}/comments
     * Thêm comment vào task
     */
    @PostMapping("/api/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse> addComment(
            @PathVariable Long taskId,
            @RequestBody TaskCommentRequest req) {
        try {
            TaskCommentDto comment = taskCommentService.addComment(taskId, req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Thêm comment thành công", comment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/tasks/{taskId}/comments
     * Lấy tất cả comment của một task
     */
    @GetMapping("/api/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse> getComments(@PathVariable Long taskId) {
        try {
            List<TaskCommentDto> comments = taskCommentService.getCommentsByTask(taskId);
            return ResponseEntity.ok(new ApiResponse(true, "OK", comments));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}

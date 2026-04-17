package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.model.Submission;
import srpm.model.Student;
import srpm.model.User;
import srpm.repository.GroupMemberRepository;
import srpm.repository.UserRepository;
import srpm.service.SubmissionService;

import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "http://localhost:5173")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Autowired
    public SubmissionController(
            SubmissionService submissionService,
            UserRepository userRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.submissionService = submissionService;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    /**
     * Sinh viên nộp bài cho một Issue
     * POST /api/submissions
     *
     * Form data:
     * - issueId: ID của Issue
     * - content: Nội dung bài nộp (link hoặc text)
     */
    @PostMapping
    public ResponseEntity<ApiResponse> submitIssue(
            @RequestParam Long issueId,
            @RequestParam String content
    ) {
        try {
            // ========== Lấy thông tin người nộp từ JWT ==========
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(
                        false,
                        "Chưa xác thực",
                        null
                ));
            }

            String username = authentication.getName();
            var userOptional = userRepository.findByUsernameOrEmail(username, username);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(
                        false,
                        "User không tồn tại",
                        null
                ));
            }

            User user = userOptional.get();

            // Chỉ Student mới được nộp bài
            if (!(user instanceof Student)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false,
                        "Chỉ sinh viên được phép nộp bài",
                        null
                ));
            }

            Student student = (Student) user;

            // ========== Lấy GroupMember của student ==========
            // Giả sử student chỉ thuộc 1 group (hoặc lấy group từ context)
            var groupMembers = groupMemberRepository.findByStudent(student.getID());
            if (groupMembers.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Sinh viên không thuộc nhóm nào",
                        null
                ));
            }

            // Lấy member đầu tiên (hoặc có thể thêm groupId vào request)
            var groupMember = groupMembers.get(0);

            // ========== Gọi service để xử lý submission ==========
            Submission submission = submissionService.submitForIssue(
                    issueId,
                    groupMember.getId(),
                    content
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                    true,
                    "Nộp bài thành công",
                    Map.of(
                            "submissionId", submission.getId(),
                            "submissionCode", submission.getSubmissionCode(),
                            "issueId", submission.getIssue().getId(),
                            "content", submission.getContent(),
                            "submittedAt", submission.getSubmittedAt()
                    )
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi nộp bài: " + e.getMessage(),
                    null
            ));
        }
    }
}


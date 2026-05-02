package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.model.Student;
import srpm.model.User;
import srpm.service.IAuthorizationService;
import srpm.service.ISubmissionService;

import java.util.*;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "http://localhost:5173")
public class SubmissionController {

    private final ISubmissionService submissionService;
    private final IAuthorizationService authorizationService;

    @Autowired
    public SubmissionController(
            ISubmissionService submissionService,
            IAuthorizationService authorizationService
    ) {
        this.submissionService = submissionService;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> submitIssue(
            @RequestParam Long issueId,
            @RequestParam String content
    ) {
        try {
            User user = authorizationService.getCurrentUser()
                    .orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(
                        false, "Chưa xác thực", null));
            }

            if (!(user instanceof Student student)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(
                        false, "Chỉ sinh viên được phép nộp bài", null));
            }

            var submission = submissionService.submitIssue(issueId, content, student);

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
                    false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false, "Lỗi nộp bài: " + e.getMessage(), null));
        }
    }

    @GetMapping("/check/{issueId}")
    public ResponseEntity<ApiResponse> checkSubmission(@PathVariable Long issueId) {
        try {
            boolean hasSubmission = submissionService.isIssueSubmitted(issueId);
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    hasSubmission ? "Issue đã được nộp bài" : "Issue chưa được nộp bài",
                    Map.of("submitted", hasSubmission)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(
                    false,
                    "Lỗi kiểm tra: " + e.getMessage(),
                    null
            ));
        }
    }
}


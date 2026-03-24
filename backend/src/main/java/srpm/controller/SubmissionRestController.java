package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.model.Submission;
import srpm.service.SubmissionService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "http://localhost:5173")
public class SubmissionRestController {

    private final SubmissionService submissionService;

    @Autowired
    public SubmissionRestController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Submission submission) {

        Map<String, Object> response = new HashMap<>();

        // Kiểm tra dữ liệu đầu vào
        if (submission.getStudentId() == null) {
            response.put("message", "Student ID is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (submission.getProjectId() == null) {
            response.put("message", "Project ID is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (submission.getFileUrl() == null || submission.getFileUrl().trim().isEmpty()) {
            response.put("message", "File URL is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            submissionService.submit(submission);

            response.put("message", "Submission successful");
            response.put("data", submission);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("message", "Submission failed");
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
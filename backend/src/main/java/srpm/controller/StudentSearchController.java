package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.dto.StudentSearchDto;
import srpm.service.IStudentService;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentSearchController {

    private final IStudentService studentService;

    @Autowired
    public StudentSearchController(IStudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> searchStudents(@RequestParam String q) {
        try {
            List<StudentSearchDto> results = studentService.searchStudents(q);
            return ResponseEntity.ok(new ApiResponse(true, "OK", results));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> getAllStudents() {
        try {
            List<StudentSearchDto> results = studentService.getAllStudents();
            return ResponseEntity.ok(new ApiResponse(true, "OK", results));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}

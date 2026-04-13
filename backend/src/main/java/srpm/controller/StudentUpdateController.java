package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.StudentManagementRequest;
import srpm.dto.response.ApiResponse;
import srpm.model.Student;
import srpm.model.UserRole;
import srpm.repository.StudentRepository;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/students")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class StudentUpdateController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StudentUpdateController(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createStudent(@RequestBody StudentManagementRequest request) {
        try {
            validateRequest(request);

            if (studentRepository.existsByUsername(request.getUsername().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Username đã tồn tại", null));
            }
            if (studentRepository.existsByEmail(request.getEmail().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email đã tồn tại", null));
            }
            if (studentRepository.existsByStudentCode(request.getStudentCode().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã sinh viên đã tồn tại", null));
            }

            Student student = new Student(
                    request.getUsername().trim(),
                    passwordEncoder.encode(request.getPassword().trim()),
                    request.getEmail().trim(),
                    UserRole.STUDENT,
                    request.getStudentCode().trim()
            );

            if (request.getJiraAccountId() != null && !request.getJiraAccountId().trim().isEmpty()) {
                student.setJiraAccountId(request.getJiraAccountId().trim());
            }
            if (request.getGithubUsername() != null && !request.getGithubUsername().trim().isEmpty()) {
                student.setGithubUsername(request.getGithubUsername().trim());
            }

            studentRepository.save(student);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tạo sinh viên thành công", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateStudent(@PathVariable Long id, @RequestBody StudentManagementRequest request) {
        try {
            validateRequest(request);

            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sinh viên"));

            // Check username exists
            if (studentRepository.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Username đã tồn tại", null));
            }

            // Check email exists
            if (studentRepository.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email đã tồn tại", null));
            }

            // Check student code exists
            if (!student.getStudentCode().equals(request.getStudentCode().trim()) &&
                studentRepository.existsByStudentCode(request.getStudentCode().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã sinh viên đã tồn tại", null));
            }

            // Update student
            student.setUsername(request.getUsername().trim());
            student.setPassword(passwordEncoder.encode(request.getPassword().trim()));
            student.setEmail(request.getEmail().trim());
            student.setStudentCode(request.getStudentCode().trim());

            if (request.getJiraAccountId() != null && !request.getJiraAccountId().trim().isEmpty()) {
                student.setJiraAccountId(request.getJiraAccountId().trim());
            }
            if (request.getGithubUsername() != null && !request.getGithubUsername().trim().isEmpty()) {
                student.setGithubUsername(request.getGithubUsername().trim());
            }

            studentRepository.save(student);

            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin sinh viên thành công", null));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    private void validateRequest(StudentManagementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password không được để trống");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (request.getStudentCode() == null || request.getStudentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sinh viên không được để trống");
        }
    }
}


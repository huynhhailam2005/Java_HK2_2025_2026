package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.LecturerManagementRequest;
import srpm.dto.response.ApiResponse;
import srpm.model.Lecturer;
import srpm.model.UserRole;
import srpm.repository.LecturerRepository;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/lecturers")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class LecturerController {

    private final LecturerRepository lecturerDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LecturerController(LecturerRepository lecturerDao, PasswordEncoder passwordEncoder) {
        this.lecturerDao = lecturerDao;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createLecturer(@RequestBody LecturerManagementRequest request) {
        try {
            validateRequest(request);

            if (lecturerDao.existsByUsername(request.getUsername().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Username đã tồn tại", null));
            }
            if (lecturerDao.existsByEmail(request.getEmail().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email đã tồn tại", null));
            }
            if (lecturerDao.existsByLecturerCode(request.getLecturerCode().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã giảng viên đã tồn tại", null));
            }

            Lecturer lecturer = new Lecturer(
                    request.getUsername().trim(),
                    passwordEncoder.encode(request.getPassword().trim()),
                    request.getEmail().trim(),
                    UserRole.LECTURER,
                    request.getLecturerCode().trim()
            );

            lecturerDao.save(lecturer);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tạo giảng viên thành công", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateLecturer(@PathVariable Long id, @RequestBody LecturerManagementRequest request) {
        try {
            validateRequest(request);

            Lecturer lecturer = lecturerDao.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy giảng viên"));

            if (lecturerDao.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Username đã tồn tại", null));
            }

            if (lecturerDao.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email đã tồn tại", null));
            }

            if (!lecturer.getLecturerCode().equals(request.getLecturerCode().trim()) &&
                lecturerDao.existsByLecturerCode(request.getLecturerCode().trim())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã giảng viên đã tồn tại", null));
            }

            lecturer.setUsername(request.getUsername().trim());
            lecturer.setPassword(passwordEncoder.encode(request.getPassword().trim()));
            lecturer.setEmail(request.getEmail().trim());
            lecturer.setLecturerCode(request.getLecturerCode().trim());

            lecturerDao.save(lecturer);

            return ResponseEntity.ok(new ApiResponse(true, "Cập nhật thông tin giảng viên thành công", null));

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

    private void validateRequest(LecturerManagementRequest request) {
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
        if (request.getLecturerCode() == null || request.getLecturerCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảng viên không được để trống");
        }
    }
}


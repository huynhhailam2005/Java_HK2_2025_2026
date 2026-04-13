package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.UpdateUserRequest;
import srpm.dto.request.UpdateLecturerRequest;
import srpm.dto.request.UpdateStudentRequest;
import srpm.model.User;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.repository.UserRepository;
import srpm.repository.LecturerRepository;
import srpm.repository.StudentRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$.*";

    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, LecturerRepository lecturerRepository,
                      StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.lecturerRepository = lecturerRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Optional<User> login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            System.out.println("[DEBUG] Login failed: null or empty parameters");
            return Optional.empty();
        }
        User user = userRepository.findByUsernameOrEmail(username.trim(), username.trim()).orElse(null);
        System.out.println("[DEBUG] User lookup result: " + (user != null ? user.getUsername() + " (role=" + user.getRole() + ")" : "NOT FOUND"));

        if (user != null && matchesAndUpgradeLegacyPasswordIfNeeded(user, password)) {
            System.out.println("[DEBUG] Password matched successfully");
            return Optional.of(user);
        }
        System.out.println("[DEBUG] Password check failed");
        return Optional.empty();
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
        if (userRepository.existsByEmail(user.getEmail())) throw new IllegalArgumentException("Email đã tồn tại");

        user.setPassword(encodePasswordIfNeeded(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        user.setPassword(encodePasswordIfNeeded(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserInfo(Long userId, UpdateUserRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User không tồn tại");
        }

        User user = userOpt.get();

        // Kiểm tra username trùng (ngoại trừ user hiện tại)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại");
            }
            user.setUsername(request.getUsername());
        }

        // Kiểm tra email trùng (ngoại trừ user hiện tại)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật password nếu có
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(encodePasswordIfNeeded(request.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public Lecturer updateLecturerInfo(Long userId, UpdateLecturerRequest request) {
        Optional<Lecturer> lecturerOpt = lecturerRepository.findByUserId(userId);
        if (!lecturerOpt.isPresent()) {
            throw new IllegalArgumentException("Lecturer không tồn tại");
        }

        Lecturer lecturer = lecturerOpt.get();

        // Kiểm tra username trùng
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(lecturer.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại");
            }
            lecturer.setUsername(request.getUsername());
        }

        // Kiểm tra email trùng
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(lecturer.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            lecturer.setEmail(request.getEmail());
        }

        // Cập nhật password
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            lecturer.setPassword(encodePasswordIfNeeded(request.getPassword()));
        }

        // Cập nhật lecturer code
        if (request.getLecturerId() != null && !request.getLecturerId().trim().isEmpty()) {
            lecturer.setLecturerCode(request.getLecturerId());
        }

        return lecturerRepository.save(lecturer);
    }

    @Transactional
    public Student updateStudentInfo(Long userId, UpdateStudentRequest request) {
        Optional<Student> studentOpt = studentRepository.findByUserId(userId);
        if (!studentOpt.isPresent()) {
            throw new IllegalArgumentException("Student không tồn tại");
        }

        Student student = studentOpt.get();

        // Kiểm tra username trùng
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(student.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại");
            }
            student.setUsername(request.getUsername());
        }

        // Kiểm tra email trùng
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(student.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            student.setEmail(request.getEmail());
        }

        // Cập nhật password
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            student.setPassword(encodePasswordIfNeeded(request.getPassword()));
        }

        // Cập nhật student code
        if (request.getStudentId() != null && !request.getStudentId().trim().isEmpty()) {
            student.setStudentCode(request.getStudentId());
        }

        // Cập nhật Jira Account ID
        if (request.getJiraAccountId() != null && !request.getJiraAccountId().trim().isEmpty()) {
            student.setJiraAccountId(request.getJiraAccountId());
        }

        // Cập nhật GitHub Username
        if (request.getGithubUsername() != null && !request.getGithubUsername().trim().isEmpty()) {
            student.setGithubUsername(request.getGithubUsername());
        }

        return studentRepository.save(student);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private boolean matchesAndUpgradeLegacyPasswordIfNeeded(User user, String rawPassword) {
        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.isBlank()) {
            System.out.println("[DEBUG] Stored password is null or blank");
            return false;
        }

        if (storedPassword.matches(BCRYPT_PATTERN)) {
            System.out.println("[DEBUG] Password is bcrypt format, checking...");
            boolean matches = passwordEncoder.matches(rawPassword, storedPassword);
            System.out.println("[DEBUG] Bcrypt match result: " + matches);
            return matches;
        }

        if (rawPassword.equals(storedPassword)) {
            System.out.println("[DEBUG] Password matches as plain text, upgrading to bcrypt...");
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }

        System.out.println("[DEBUG] Password does not match");
        return false;
    }

    private String encodePasswordIfNeeded(String rawOrHashedPassword) {
        if (rawOrHashedPassword == null || rawOrHashedPassword.isBlank()) {
            throw new IllegalArgumentException("Password không được để trống");
        }

        if (rawOrHashedPassword.matches(BCRYPT_PATTERN)) {
            return rawOrHashedPassword;
        }

        return passwordEncoder.encode(rawOrHashedPassword);
    }
}
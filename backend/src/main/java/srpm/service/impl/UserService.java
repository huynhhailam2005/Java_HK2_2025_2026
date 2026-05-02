package srpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.RegisterRequest;
import srpm.dto.request.UpdateUserRequest;
import srpm.dto.request.UpdateLecturerRequest;
import srpm.dto.request.UpdateStudentRequest;
import srpm.dto.response.LecturerResponse;
import srpm.dto.response.StudentResponse;
import srpm.exception.ValidationException;
import srpm.model.*;
import srpm.repository.ILecturerRepository;
import srpm.repository.IStudentRepository;
import srpm.repository.IUserRepository;
import srpm.service.IUserService;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService implements IUserService {

    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$.*";
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final IUserRepository IUserRepository;
    private final ILecturerRepository ILecturerRepository;
    private final IStudentRepository IStudentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(IUserRepository IUserRepository, ILecturerRepository ILecturerRepository,
                       IStudentRepository IStudentRepository, PasswordEncoder passwordEncoder) {
        this.IUserRepository = IUserRepository;
        this.ILecturerRepository = ILecturerRepository;
        this.IStudentRepository = IStudentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest regRequest) {
        logger.debug("Service register attempt: username={}, email={}", regRequest.getUsername(), regRequest.getEmail());

        String generatedCode = UUID.randomUUID().toString().substring(0, 20);

        User user = UserFactory.createUser(regRequest.getRole());
        if (user == null) {
            throw new ValidationException("Role không hợp lệ: " + regRequest.getRole());
        }

        user.setUsername(regRequest.getUsername());
        user.setPassword(regRequest.getPassword());
        user.setEmail(regRequest.getEmail());

        if (user instanceof Admin) {
            ((Admin) user).setAdminCode(generatedCode);
        } else if (user instanceof Lecturer) {
            ((Lecturer) user).setLecturerCode(generatedCode);
        } else if (user instanceof Student) {
            ((Student) user).setStudentCode(generatedCode);
        }

        return createUser(user);
    }

    @Transactional
    public Optional<User> login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            logger.debug("Login failed: null or empty parameters");
            return Optional.empty();
        }
        User user = IUserRepository.findByUsernameOrEmail(username.trim(), username.trim()).orElse(null);
        logger.debug("User lookup result: {}", user != null ? user.getUsername() + " (role=" + user.getRole() + ")" : "NOT FOUND");

        if (user != null && matchesAndUpgradeLegacyPasswordIfNeeded(user, password)) {
            logger.info("User login successful: {}", user.getUsername());
            return Optional.of(user);
        }
        logger.warn("Login failed for username: {}", username);
        return Optional.empty();
    }

    @Transactional
    public User createUser(User user) {
        if (IUserRepository.existsByUsername(user.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
        if (IUserRepository.existsByEmail(user.getEmail())) throw new IllegalArgumentException("Email đã tồn tại");

        user.setPassword(encodePasswordIfNeeded(user.getPassword()));
        return IUserRepository.save(user);
    }

    @Transactional
    public User updateUserInfo(Long userId, UpdateUserRequest request) {
        Optional<User> userOpt = IUserRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User không tồn tại");
        }

        User user = userOpt.get();

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(user.getUsername()) &&
                IUserRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail()) &&
                IUserRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            user.setEmail(request.getEmail());
        }

        return IUserRepository.save(user);
    }

    @Transactional
    public Lecturer updateLecturerInfo(Long userId, UpdateLecturerRequest request) {
        Optional<Lecturer> lecturerOpt = ILecturerRepository.findByUserId(userId);
        if (!lecturerOpt.isPresent()) {
            throw new IllegalArgumentException("Lecturer không tồn tại");
        }

        Lecturer lecturer = lecturerOpt.get();

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(lecturer.getUsername()) &&
                IUserRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại");
            }
            lecturer.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(lecturer.getEmail()) &&
                IUserRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            lecturer.setEmail(request.getEmail());
        }

        if (request.getLecturerId() != null && !request.getLecturerId().trim().isEmpty()) {
            lecturer.setLecturerCode(request.getLecturerId());
        }

        return ILecturerRepository.save(lecturer);
    }

    @Transactional
    public Student updateStudentInfo(Long userId, UpdateStudentRequest request) {
        Optional<Student> studentOpt = IStudentRepository.findByUserId(userId);
        if (!studentOpt.isPresent()) {
            throw new IllegalArgumentException("Student không tồn tại");
        }

        Student student = studentOpt.get();

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(student.getUsername()) &&
                IUserRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại");
            }
            student.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(student.getEmail()) &&
                IUserRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            student.setEmail(request.getEmail());
        }

        if (request.getStudentId() != null && !request.getStudentId().trim().isEmpty()) {
            student.setStudentCode(request.getStudentId());
        }

        if (request.getJiraAccountId() != null && !request.getJiraAccountId().trim().isEmpty()) {
            student.setJiraAccountId(request.getJiraAccountId());
        }

        if (request.getGithubUsername() != null && !request.getGithubUsername().trim().isEmpty()) {
            student.setGithubUsername(request.getGithubUsername());
        }

        return IStudentRepository.save(student);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = IUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu cũ không được để trống");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống");
        }

        if (!matchesAndUpgradeLegacyPasswordIfNeeded(user, oldPassword)) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        user.setPassword(encodePasswordIfNeeded(newPassword));
        IUserRepository.save(user);

        logger.info("Password changed successfully for user: {}", user.getUsername());
    }

    public Optional<User> getUserById(Long id) {
        return IUserRepository.findById(id);
    }

    public Object toUserResponse(User user) {
        if (user instanceof Student) {
            Long userId = user.getID();
            Student student = IStudentRepository.findByUserId(userId).orElse((Student) user);
            return new StudentResponse(
                    student.getID(), student.getUsername(), student.getEmail(),
                    student.getRole(), student.getStudentCode(),
                    student.getJiraAccountId(), student.getGithubUsername()
            );
        } else if (user instanceof Lecturer) {
            Long userId = user.getID();
            Lecturer lecturer = ILecturerRepository.findByUserId(userId).orElse((Lecturer) user);
            return new LecturerResponse(
                    lecturer.getID(), lecturer.getUsername(), lecturer.getEmail(),
                    lecturer.getRole(), lecturer.getLecturerCode()
            );
        }
        return user;
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
            IUserRepository.save(user);
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
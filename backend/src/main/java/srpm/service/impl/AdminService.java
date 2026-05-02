package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.AdminRequest;
import srpm.dto.request.LecturerRequest;
import srpm.dto.request.StudentRequest;
import srpm.dto.response.AdminResponse;
import srpm.model.*;
import srpm.repository.ILecturerRepository;
import srpm.repository.IStudentRepository;
import srpm.repository.IUserRepository;
import srpm.service.IAdminService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService implements IAdminService {

    private final IUserRepository userDao;
    private final IStudentRepository studentDao;
    private final ILecturerRepository lecturerDao;
    private final PasswordEncoder passwordEncoder;
    private final List<UserRole> managedUserRoles;

    @Autowired
    public AdminService(
            IUserRepository userDao,
            IStudentRepository studentDao,
            ILecturerRepository lecturerDao,
            PasswordEncoder passwordEncoder,
            @Value("${admin.user.managed-roles:LECTURER}") String managedRolesConfig
    ) {
        this.userDao = userDao;
        this.studentDao = studentDao;
        this.lecturerDao = lecturerDao;
        this.passwordEncoder = passwordEncoder;
        this.managedUserRoles = parseManagedRolesConfig(managedRolesConfig);
    }

    public List<User> getManagedUsers(String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return userDao.findAllByUserRoleInOrderByUsernameAsc(managedUserRoles);
        }

        UserRole userRole = parseManagedRole(roleFilter);
        return userDao.findAllByUserRoleOrderByUsernameAsc(userRole);
    }

    public User getManagedUserById(Long id) {
        User user = userDao.findByIdAndUserRoleIn(id, managedUserRoles).orElse(null);
        if (user == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }
        return user;
    }


    @Transactional
    public User updateManagedUser(Long id, AdminRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }

        User existing = userDao.findByIdAndUserRoleIn(id, managedUserRoles).orElse(null);
        if (existing == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }

        if (userDao.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userDao.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        existing.setUsername(request.getUsername().trim());
        existing.setEmail(request.getEmail().trim());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("Password phải tối thiểu 6 ký tự");
            }
            existing.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        return userDao.save(existing);
    }

    @Transactional
    public void deleteManagedUser(Long id) {
        User existing = userDao.findByIdAndUserRoleIn(id, managedUserRoles).orElse(null);
        if (existing == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }

        userDao.deleteById(id);
    }

    private UserRole parseManagedRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role không được để trống");
        }

        UserRole parsed;
        try {
            parsed = UserRole.fromValue(role.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role không hợp lệ. Chỉ chấp nhận: " + managedRolesText());
        }
        if (!managedUserRoles.contains(parsed)) {
            throw new IllegalArgumentException("Admin chỉ được quản lý: " + managedRolesText());
        }

        return parsed;
    }

    @Transactional
    @Override
    public User createStudent(StudentRequest request) {
        validateStudentRequest(request, true);

        if (studentDao.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (studentDao.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (studentDao.existsByStudentCode(request.getStudentCode().trim())) {
            throw new IllegalArgumentException("Mã sinh viên đã tồn tại");
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

        return studentDao.save(student);
    }

    @Transactional
    @Override
    public User updateStudent(Long id, StudentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        validateStudentRequest(request, false);

        Student student = studentDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sinh viên"));

        if (studentDao.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (studentDao.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (!student.getStudentCode().equals(request.getStudentCode().trim()) &&
            studentDao.existsByStudentCode(request.getStudentCode().trim())) {
            throw new IllegalArgumentException("Mã sinh viên đã tồn tại");
        }

        student.setUsername(request.getUsername().trim());
        student.setEmail(request.getEmail().trim());
        student.setStudentCode(request.getStudentCode().trim());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("Password phải tối thiểu 6 ký tự");
            }
            student.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        if (request.getJiraAccountId() != null && !request.getJiraAccountId().trim().isEmpty()) {
            student.setJiraAccountId(request.getJiraAccountId().trim());
        }
        if (request.getGithubUsername() != null && !request.getGithubUsername().trim().isEmpty()) {
            student.setGithubUsername(request.getGithubUsername().trim());
        }

        return studentDao.save(student);
    }

    @Transactional
    @Override
    public User createLecturer(LecturerRequest request) {
        validateLecturerRequest(request, true);

        if (lecturerDao.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (lecturerDao.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (lecturerDao.existsByLecturerCode(request.getLecturerCode().trim())) {
            throw new IllegalArgumentException("Mã giảng viên đã tồn tại");
        }

        Lecturer lecturer = new Lecturer(
                request.getUsername().trim(),
                passwordEncoder.encode(request.getPassword().trim()),
                request.getEmail().trim(),
                UserRole.LECTURER,
                request.getLecturerCode().trim()
        );

        return lecturerDao.save(lecturer);
    }

    @Transactional
    @Override
    public User updateLecturer(Long id, LecturerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        validateLecturerRequest(request, false);

        Lecturer lecturer = lecturerDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy giảng viên"));

        if (lecturerDao.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (lecturerDao.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (!lecturer.getLecturerCode().equals(request.getLecturerCode().trim()) &&
            lecturerDao.existsByLecturerCode(request.getLecturerCode().trim())) {
            throw new IllegalArgumentException("Mã giảng viên đã tồn tại");
        }

        lecturer.setUsername(request.getUsername().trim());
        lecturer.setEmail(request.getEmail().trim());
        lecturer.setLecturerCode(request.getLecturerCode().trim());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("Password phải tối thiểu 6 ký tự");
            }
            lecturer.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        return lecturerDao.save(lecturer);
    }

    private void validateStudentRequest(StudentRequest request, boolean requirePassword) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (requirePassword && (request.getPassword() == null || request.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("Password không được để trống");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (request.getStudentCode() == null || request.getStudentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sinh viên không được để trống");
        }
    }

    private void validateLecturerRequest(LecturerRequest request, boolean requirePassword) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (requirePassword && (request.getPassword() == null || request.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("Password không được để trống");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (request.getLecturerCode() == null || request.getLecturerCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giảng viên không được để trống");
        }
    }

    private String managedRolesText() {
        return managedUserRoles.stream()
                .map(role -> role.name().toUpperCase())
                .collect(Collectors.joining(", "));
    }

    @Override
    public Object toAdminResponse(User user) {
        String studentCode = null;
        String lecturerCode = null;
        String jiraAccountId = null;
        String githubUsername = null;

        if (user instanceof Student student) {
            studentCode = student.getStudentCode();
            jiraAccountId = student.getJiraAccountId();
            githubUsername = student.getGithubUsername();
        } else if (user instanceof Lecturer lecturer) {
            lecturerCode = lecturer.getLecturerCode();
        } else if (user instanceof Admin admin) {
            lecturerCode = admin.getAdminCode();
        }

        return new AdminResponse(
                user.getID(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                studentCode,
                lecturerCode,
                githubUsername,
                jiraAccountId
        );
    }

    private List<UserRole> parseManagedRolesConfig(String managedRolesConfig) {
        List<UserRole> userRoles = java.util.Arrays.stream(managedRolesConfig.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .map(token -> {
                    try {
                        return UserRole.valueOf(token.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalStateException("Cấu hình admin.user.managed-roles không hợp lệ: " + token);
                    }
                })
                .toList();

        if (userRoles.isEmpty()) {
            throw new IllegalStateException("Cấu hình admin.user.managed-roles không được để trống");
        }

        if (userRoles.contains(UserRole.ADMIN)) {
            throw new IllegalStateException("Không cho phép ADMIN trong admin.user.managed-roles");
        }

        return userRoles;
    }
}

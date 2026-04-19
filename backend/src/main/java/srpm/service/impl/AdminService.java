package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.AdminRequest;
import srpm.model.*;
import srpm.repository.UserRepository;
import srpm.service.IAdminService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService implements IAdminService {

    private final UserRepository userDao;
    private final PasswordEncoder passwordEncoder;
    private final List<UserRole> managedUserRoles;

    @Autowired
    public AdminService(
            UserRepository userDao,
            PasswordEncoder passwordEncoder,
            @Value("${admin.user.managed-roles:LECTURER}") String managedRolesConfig
    ) {
        this.userDao = userDao;
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
        validateRequest(request);

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

        // Update existing user instead of creating new one
        existing.setUsername(request.getUsername().trim());
        existing.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        existing.setEmail(request.getEmail().trim());

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

    private void validateRequest(AdminRequest request) {
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
    }

    // ...existing code...

    private String managedRolesText() {
        return managedUserRoles.stream()
                .map(role -> role.name().toUpperCase())
                .collect(Collectors.joining(", "));
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

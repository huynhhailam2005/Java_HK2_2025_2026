package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.AdminUserManagementRequest;
import srpm.model.*;
import srpm.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final List<UserRole> managedUserRoles;

    @Autowired
    public AdminUserManagementService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.user.managed-roles:LECTURER}") String managedRolesConfig
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.managedUserRoles = parseManagedRolesConfig(managedRolesConfig);
    }

    public List<User> getManagedUsers(String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return userRepository.findAllByUserRoleInOrderByUsernameAsc(managedUserRoles);
        }

        UserRole userRole = parseManagedRole(roleFilter);
        return userRepository.findAllByUserRoleOrderByUsernameAsc(userRole);
    }

    public User getManagedUserById(Long id) {
        User user = userRepository.findByIdAndUserRoleIn(id, managedUserRoles).orElse(null);
        if (user == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }
        return user;
    }


    @Transactional
    public User updateManagedUser(Long id, AdminUserManagementRequest request) {
        validateRequest(request);

        User existing = userRepository.findByIdAndUserRoleIn(id, managedUserRoles).orElse(null);
        if (existing == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }

        if (userRepository.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Update existing user instead of creating new one
        existing.setUsername(request.getUsername().trim());
        existing.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        existing.setEmail(request.getEmail().trim());

        return userRepository.save(existing);
    }

    @Transactional
    public void deleteManagedUser(Long id) {
        User existing = userRepository.findByIdAndUserRoleIn(id, managedUserRoles).orElse(null);
        if (existing == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }

        userRepository.deleteById(id);
    }

    private UserRole parseManagedRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role không được để trống");
        }

        UserRole parsed;
        try {
            parsed = UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role không hợp lệ. Chỉ chấp nhận: " + managedRolesText());
        }
        if (!managedUserRoles.contains(parsed)) {
            throw new IllegalArgumentException("Admin chỉ được quản lý: " + managedRolesText());
        }

        return parsed;
    }

    private void validateRequest(AdminUserManagementRequest request) {
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

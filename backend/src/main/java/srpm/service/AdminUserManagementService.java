package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.dto.request.AdminUserManagementRequest;
import srpm.model.Role;
import srpm.model.User;
import srpm.model.UserFactory;
import srpm.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final List<Role> managedRoles;

    @Autowired
    public AdminUserManagementService(
            UserRepository userRepository,
            @Value("${admin.user.managed-roles:LECTURER}") String managedRolesConfig
    ) {
        this.userRepository = userRepository;
        this.managedRoles = parseManagedRolesConfig(managedRolesConfig);
    }

    public List<User> getManagedUsers(String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return userRepository.findAllByRoleInOrderByUsernameAsc(managedRoles);
        }

        Role role = parseManagedRole(roleFilter);
        return userRepository.findAllByRoleOrderByUsernameAsc(role);
    }

    public User getManagedUserById(String id) {
        User user = userRepository.findByIdAndRoleIn(id, managedRoles).orElse(null);
        if (user == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }
        return user;
    }

    @Transactional
    public User createManagedUser(AdminUserManagementRequest request) {
        validateRequest(request);

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role role = parseManagedRole(request.getRole());
        User user = buildUserFromRequest(request, null, role);
        user.setID(UUID.randomUUID().toString());

        return userRepository.save(user);
    }

    @Transactional
    public User updateManagedUser(String id, AdminUserManagementRequest request) {
        validateRequest(request);

        User existing = userRepository.findByIdAndRoleIn(id, managedRoles).orElse(null);
        if (existing == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }

        if (userRepository.existsByUsernameAndIdNot(request.getUsername().trim(), id)) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role role = parseManagedRole(request.getRole());
        User userToUpdate = buildUserFromRequest(request, id, role);

        return userRepository.save(userToUpdate);
    }

    @Transactional
    public void deleteManagedUser(String id) {
        User existing = userRepository.findByIdAndRoleIn(id, managedRoles).orElse(null);
        if (existing == null) {
            throw new NoSuchElementException("Không tìm thấy người dùng");
        }

        userRepository.deleteById(id);
    }

    private Role parseManagedRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role không được để trống");
        }

        Role parsed;
        try {
            parsed = Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role không hợp lệ. Chỉ chấp nhận: " + managedRolesText());
        }
        if (!managedRoles.contains(parsed)) {
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
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role không được để trống");
        }
    }

    private User buildUserFromRequest(AdminUserManagementRequest request, String id, Role role) {
        User user = UserFactory.createUser(role.name());
        user.setRole(role);
        user.setID(id);
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword().trim());
        user.setEmail(request.getEmail().trim());
        return user;
    }

    private String managedRolesText() {
        return managedRoles.stream()
                .map(role -> role.name().toUpperCase())
                .collect(Collectors.joining(", "));
    }

    private List<Role> parseManagedRolesConfig(String managedRolesConfig) {
        List<Role> roles = List.of(managedRolesConfig.split(","))
                .stream()
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .map(token -> {
                    try {
                        return Role.valueOf(token.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalStateException("Cấu hình admin.user.managed-roles không hợp lệ: " + token);
                    }
                })
                .toList();

        if (roles.isEmpty()) {
            throw new IllegalStateException("Cấu hình admin.user.managed-roles không được để trống");
        }

        if (roles.contains(Role.ADMIN)) {
            throw new IllegalStateException("Không cho phép ADMIN trong admin.user.managed-roles");
        }

        return roles;
    }
}

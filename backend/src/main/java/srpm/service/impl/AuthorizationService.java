package srpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import srpm.exception.ForbiddenException;
import srpm.exception.ResourceNotFoundException;
import srpm.exception.UnauthorizedException;
import srpm.model.Admin;
import srpm.model.Group;
import srpm.model.GroupMemberRole;
import srpm.model.Lecturer;
import srpm.model.Student;
import srpm.model.User;
import srpm.repository.GroupRepository;
import srpm.repository.UserRepository;
import srpm.service.IAuthorizationService;

import java.util.Optional;

@Service
public class AuthorizationService implements IAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public AuthorizationService(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    // Lấy user hiện tại từ security context
    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("No authenticated user found in SecurityContext");
            return Optional.empty();
        }

        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsernameOrEmail(username, username);

        if (user.isPresent()) {
            logger.debug("Current user: {} (role: {})", username, user.get().getRole());
        } else {
            logger.warn("Authenticated user {} not found in database", username);
        }

        return user;
    }

    // Kiểm tra user có quyền truy cập group: Admin > Lecturer > Student members
    @Override
    public boolean canAccessGroup(Long groupId) {
        Optional<User> userOpt = getCurrentUser();

        if (userOpt.isEmpty()) {
            logger.warn("Cannot access group {}: user not authenticated", groupId);
            throw new UnauthorizedException("Bạn phải đăng nhập trước");
        }

        User user = userOpt.get();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm không tồn tại: " + groupId));

        // Admin truy cập tất cả
        if (user instanceof Admin) {
            logger.debug("Admin {} can access group {}", user.getUsername(), groupId);
            return true;
        }

        // Lecturer truy cập group mình dạy
        if (user instanceof Lecturer) {
            Lecturer lecturer = (Lecturer) user;
            boolean hasAccess = group.getLecturer().getId().equals(lecturer.getId());
            logger.debug("Lecturer {} access to group {}: {}", lecturer.getUsername(), groupId, hasAccess);
            return hasAccess;
        }

        // Student truy cập group mình là member
        if (user instanceof Student) {
            Student student = (Student) user;
            boolean hasAccess = group.getGroupMembers().stream()
                    .anyMatch(gm -> gm.getStudent().getId().equals(student.getId()));
            logger.debug("Student {} access to group {}: {}", student.getUsername(), groupId, hasAccess);
            return hasAccess;
        }

        logger.warn("Unknown user type: {} cannot access group {}", user.getClass().getName(), groupId);
        return false;
    }

    // Kiểm tra user có phải team leader: Admin > Lecturer owner > Student leader
    @Override
    public boolean isTeamLeaderOfGroup(Long groupId) {
        Optional<User> userOpt = getCurrentUser();

        if (userOpt.isEmpty()) {
            logger.warn("Cannot check team leader for group {}: user not authenticated", groupId);
            throw new UnauthorizedException("Bạn phải đăng nhập trước");
        }

        User user = userOpt.get();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm không tồn tại: " + groupId));

        // Admin được phép
        if (user instanceof Admin) {
            logger.debug("Admin {} is team leader for group {}", user.getUsername(), groupId);
            return true;
        }

        // Lecturer quản lý group được phép
        if (user instanceof Lecturer) {
            Lecturer lecturer = (Lecturer) user;
            boolean isTeamLeader = group.getLecturer().getId().equals(lecturer.getId());
            logger.debug("Lecturer {} is team leader for group {}: {}", lecturer.getUsername(), groupId, isTeamLeader);
            return isTeamLeader;
        }

        // Student phải là TEAM_LEADER
        if (user instanceof Student) {
            Student student = (Student) user;
            boolean isTeamLeader = group.getGroupMembers().stream()
                    .anyMatch(gm -> gm.getStudent().getId().equals(student.getId()) &&
                            gm.getGroupMemberRole() == GroupMemberRole.TEAM_LEADER);
            logger.debug("Student {} is team leader for group {}: {}", student.getUsername(), groupId, isTeamLeader);
            return isTeamLeader;
        }

        logger.warn("Unknown user type: {} cannot be team leader for group {}", user.getClass().getName(), groupId);
        return false;
    }

    // Yêu cầu user phải authenticate
    @Override
    public void requireAuthentication() {
        Optional<User> user = getCurrentUser();

        if (user.isEmpty()) {
            logger.warn("Authentication required but user not authenticated");
            throw new UnauthorizedException("Bạn phải đăng nhập trước");
        }
    }

    // Yêu cầu user có role cụ thể
    @Override
    public void requireRole(String role) {
        Optional<User> userOpt = getCurrentUser();

        if (userOpt.isEmpty()) {
            logger.warn("Role {} required but user not authenticated", role);
            throw new UnauthorizedException("Bạn phải đăng nhập trước");
        }

        User user = userOpt.get();
        String userRole = user.getRole().name();

        if (!userRole.equalsIgnoreCase(role)) {
            logger.warn("User {} has role {} but {} required", user.getUsername(), userRole, role);
            throw new ForbiddenException("Bạn không có quyền thực hiện hành động này");
        }
    }
}


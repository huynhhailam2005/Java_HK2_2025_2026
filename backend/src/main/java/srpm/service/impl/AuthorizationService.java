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
import srpm.repository.IGroupRepository;
import srpm.repository.IUserRepository;
import srpm.service.IAuthorizationService;

import java.util.Optional;

@Service
public class AuthorizationService implements IAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final IUserRepository IUserRepository;
    private final IGroupRepository IGroupRepository;

    @Autowired
    public AuthorizationService(IUserRepository IUserRepository, IGroupRepository IGroupRepository) {
        this.IUserRepository = IUserRepository;
        this.IGroupRepository = IGroupRepository;
    }

    @Override
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("No authenticated user found in SecurityContext");
            return Optional.empty();
        }

        String username = authentication.getName();
        Optional<User> user = IUserRepository.findByUsernameOrEmail(username, username);

        if (user.isPresent()) {
            logger.debug("Current user: {} (role: {})", username, user.get().getRole());
        } else {
            logger.warn("Authenticated user {} not found in database", username);
        }

        return user;
    }

    @Override
    public boolean canAccessGroup(Long groupId) {
        Optional<User> userOpt = getCurrentUser();

        if (userOpt.isEmpty()) {
            logger.warn("Cannot access group {}: user not authenticated", groupId);
            throw new UnauthorizedException("Bạn phải đăng nhập trước");
        }

        User user = userOpt.get();
        Group group = IGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm không tồn tại: " + groupId));

        if (user instanceof Admin) {
            logger.debug("Admin {} can access group {}", user.getUsername(), groupId);
            return true;
        }

        if (user instanceof Lecturer) {
            Lecturer lecturer = (Lecturer) user;
            boolean hasAccess = group.getLecturer().getId().equals(lecturer.getId());
            logger.debug("Lecturer {} access to group {}: {}", lecturer.getUsername(), groupId, hasAccess);
            return hasAccess;
        }

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

    @Override
    public void requireAuthentication() {
        Optional<User> user = getCurrentUser();

        if (user.isEmpty()) {
            logger.warn("Authentication required but user not authenticated");
            throw new UnauthorizedException("Bạn phải đăng nhập trước");
        }
    }
}


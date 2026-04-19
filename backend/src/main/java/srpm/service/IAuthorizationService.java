package srpm.service;

import srpm.model.User;

import java.util.Optional;

public interface IAuthorizationService {

    Optional<User> getCurrentUser();

    boolean canAccessGroup(Long groupId);

    boolean isTeamLeaderOfGroup(Long groupId);

    void requireAuthentication();

    void requireRole(String role);
}


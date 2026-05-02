package srpm.service;

import srpm.model.User;

import java.util.Optional;

public interface IAuthorizationService {

    Optional<User> getCurrentUser();

    boolean canAccessGroup(Long groupId);

    void requireAuthentication();
}


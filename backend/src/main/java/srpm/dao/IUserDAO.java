package srpm.dao;

import srpm.model.User;

public interface IUserDAO {
    User findById(String id);
    void deleteById(String id);

    User findByUsernameOrEmail(String identifier);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    void save(User user);
    void update(User user);
}
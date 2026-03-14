package srpm.dao;

import srpm.model.User;
import java.sql.SQLException;

public interface IUserDAO {
    void save(User user) throws SQLException, ClassNotFoundException;
    User findByUsernameOrEmail(String identifier) throws SQLException, ClassNotFoundException;
    boolean existsByUsername(String username) throws SQLException, ClassNotFoundException;
    boolean existsByEmail(String email) throws SQLException, ClassNotFoundException;
}
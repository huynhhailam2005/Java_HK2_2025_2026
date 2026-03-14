package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import srpm.dao.IUserDAO;
import srpm.model.User;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class UserService {

    private final IUserDAO userDAO;

    @Autowired
    public UserService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) return Optional.empty();
        try {
            User user = userDAO.findByUsernameOrEmail(username.trim());
            if (user != null && password.equals(user.getPassword())) {
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException | ClassNotFoundException ex) {
            throw new IllegalStateException("Lỗi DB khi đăng nhập", ex);
        }
    }

    public User createUser(User user) {
        try {
            if (userDAO.existsByUsername(user.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
            if (userDAO.existsByEmail(user.getEmail())) throw new IllegalArgumentException("Email đã tồn tại");
            userDAO.save(user);
            return user;
        } catch (SQLException | ClassNotFoundException ex) {
            throw new IllegalStateException("Lỗi DB khi tạo người dùng", ex);
        }
    }

    public User updateUser(User user) {
        try {
            userDAO.update(user);
            return user;
        } catch (SQLException | ClassNotFoundException ex) {
            throw new IllegalStateException("Lỗi DB khi cập nhật", ex);
        }
    }

    public Optional<User> getUserById(Long id) {
        try {
            return Optional.ofNullable(userDAO.findById(id));
        } catch (SQLException | ClassNotFoundException ex) {
            throw new IllegalStateException("Lỗi DB khi tìm ID", ex);
        }
    }

    public void deleteUser(Long id) {
        try {
            userDAO.deleteById(id);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new IllegalStateException("Lỗi DB khi xóa", ex);
        }
    }
}
package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import srpm.dao.IUserDAO;
import srpm.model.User;

import java.util.Optional;
import java.util.UUID;

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
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi đăng nhập", ex);
        }
    }

    public User createUser(User user) {
        try {
            if (userDAO.existsByUsername(user.getUsername())) throw new IllegalArgumentException("Username đã tồn tại");
            if (userDAO.existsByEmail(user.getEmail())) throw new IllegalArgumentException("Email đã tồn tại");
            if (user.getID() == null || user.getID().isBlank()) {
                user.setID(UUID.randomUUID().toString());
            }
            userDAO.save(user);
            return user;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi tạo người dùng", ex);
        }
    }

    public User updateUser(User user) {
        try {
            userDAO.update(user);
            return user;
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi cập nhật", ex);
        }
    }

    public Optional<User> getUserById(String id) {
        try {
            return Optional.ofNullable(userDAO.findById(id));
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi tìm ID", ex);
        }
    }

    public void deleteUser(String id) {
        try {
            userDAO.deleteById(id);
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi xóa", ex);
        }
    }
}
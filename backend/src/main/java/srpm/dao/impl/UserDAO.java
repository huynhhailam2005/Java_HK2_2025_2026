package srpm.dao.impl;

import org.springframework.stereotype.Repository;
import srpm.context.DBContext;
import srpm.dao.IUserDAO;
import srpm.model.Student;
import srpm.model.User;
import srpm.model.UserFactory;
import java.sql.*;

@Repository
public class UserDAO implements IUserDAO {

    @Override
    public void save(User user) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO users (username, password, gmail, role, student_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setString(5, (user instanceof Student) ? ((Student) user).getStudentId() : null);
            ps.executeUpdate();
        }
    }

    @Override
    public void update(User user) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE users SET username=?, password=?, gmail=?, role=?, student_id=? WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setString(5, (user instanceof Student) ? ((Student) user).getStudentId() : null);
            ps.setLong(6, Long.parseLong(user.getID())); // Giả định ID trong User model là String
            ps.executeUpdate();
        }
    }

    @Override
    public User findById(Long id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        }
        return null;
    }

    @Override
    public User findByUsernameOrEmail(String identifier) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE username = ? OR gmail = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        }
        return null;
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM users WHERE gmail = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public void deleteById(Long id) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        }
        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = UserFactory.createUser(rs.getString("role"));
        user.setID(String.valueOf(rs.getLong("id")));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("gmail"));
        if (user instanceof Student) {
            ((Student) user).setStudentId(rs.getString("student_id"));
        }
        return user;
    }
}
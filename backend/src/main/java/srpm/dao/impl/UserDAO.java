package srpm.dao.impl;

import srpm.context.DBContext;
import srpm.dao.IUserDAO;
import srpm.model.User;
import srpm.model.Student;
import srpm.model.UserFactory;
import java.sql.*;

public class UserDAO implements IUserDAO {

    @Override
    public void save(User user) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO users (id, username, password, gmail, role, student_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getID());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());

            if (user instanceof Student) {
                ps.setString(6, ((Student) user).getStudentId());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            ps.executeUpdate();
        }
    }

    @Override
    public User findByUsernameOrEmail(String identifier) throws SQLException, ClassNotFoundException {
        // Query cho phép đăng nhập bằng cả username hoặc email
        String sql = "SELECT * FROM users WHERE username = ? OR gmail = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = UserFactory.createUser(rs.getString("role"));
                    user.setID(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("gmail"));

                    if (user instanceof Student) {
                        ((Student) user).setStudentId(rs.getString("student_id"));
                    }
                    return user;
                }
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
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM users WHERE gmail = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
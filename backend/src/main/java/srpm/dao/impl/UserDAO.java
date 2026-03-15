package srpm.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import srpm.dao.IUserDAO;
import srpm.model.Student;
import srpm.model.User;
import srpm.model.UserFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserDAO implements IUserDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) return mapUser(rs);
            return null;
        }, id);
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public User findByUsernameOrEmail(String identifier) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) return mapUser(rs);
            return null;
        }, identifier, identifier);
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, username, password, email, role, student_id) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getID(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole().name(),
                (user instanceof Student) ? ((Student) user).getStudentId() : null
        );
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username=?, password=?, email=?, role=?, student_id=? WHERE id=?";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole().name(),
                (user instanceof Student) ? ((Student) user).getStudentId() : null,
                user.getID()
        );
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = UserFactory.createUser(rs.getString("role"));
        user.setID(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        if (user instanceof Student) {
            ((Student) user).setStudentId(rs.getString("student_id"));
        }
        return user;
    }
}
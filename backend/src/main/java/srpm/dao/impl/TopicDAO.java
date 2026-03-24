package srpm.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import srpm.dao.ITopicDAO;
import srpm.model.Topic;
import srpm.model.TopicStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class TopicDAO implements ITopicDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TopicDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Topic findById(String id) {
        String sql = "SELECT * FROM topics WHERE id = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) return mapTopic(rs);
            return null;
        }, id);
    }

    @Override
    public List<Topic> findAll() {
        String sql = "SELECT * FROM topics ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTopic(rs));
    }

    @Override
    public List<Topic> findByLecturerId(String lecturerId) {
        String sql = "SELECT * FROM topics WHERE lecturer_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTopic(rs), lecturerId);
    }

    @Override
    public void save(Topic topic) {
        String sql = """
                INSERT INTO topics (id, title, description, status, lecturer_id, student_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                topic.getId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getStatus().name(),
                topic.getLecturerId(),
                topic.getStudentId(),
                Timestamp.valueOf(topic.getCreatedAt()),
                Timestamp.valueOf(topic.getUpdatedAt())
        );
    }

    @Override
    public void update(Topic topic) {
        String sql = """
                UPDATE topics
                SET title = ?, description = ?, status = ?, lecturer_id = ?, student_id = ?, updated_at = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                topic.getTitle(),
                topic.getDescription(),
                topic.getStatus().name(),
                topic.getLecturerId(),
                topic.getStudentId(),
                Timestamp.valueOf(topic.getUpdatedAt()),
                topic.getId()
        );
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM topics WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    // -----------------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------------

    private Topic mapTopic(ResultSet rs) throws SQLException {
        Topic topic = new Topic();
        topic.setId(rs.getString("id"));
        topic.setTitle(rs.getString("title"));
        topic.setDescription(rs.getString("description"));
        topic.setStatus(TopicStatus.valueOf(rs.getString("status")));
        topic.setLecturerId(rs.getString("lecturer_id"));
        topic.setStudentId(rs.getString("student_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null) topic.setCreatedAt(createdAt.toLocalDateTime());
        if (updatedAt != null) topic.setUpdatedAt(updatedAt.toLocalDateTime());

        return topic;
    }
}

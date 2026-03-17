package srpm.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import srpm.dao.IFeedbackDAO;
import srpm.model.Feedback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository 
public class FeedbackDAO implements IFeedbackDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedbacks (id, content, rating, reviewer_id, group_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            feedback.getId(), 
            feedback.getContent(), 
            feedback.getRating(), 
            feedback.getReviewerId(), 
            feedback.getGroupId(), 
            feedback.getCreatedAt()
        );
    }

    @Override
    public List<Feedback> getFeedbacksByGroupId(String groupId) {
        String sql = "SELECT * FROM feedbacks WHERE group_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, this::mapRowToFeedback, groupId);
    }


    private Feedback mapRowToFeedback(ResultSet rs, int rowNum) throws SQLException {
        Feedback fb = new Feedback();
        fb.setId(rs.getString("id"));
        fb.setContent(rs.getString("content"));
        fb.setRating(rs.getInt("rating"));
        fb.setReviewerId(rs.getString("reviewer_id"));
        fb.setGroupId(rs.getString("group_id"));
        fb.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return fb;
    }
}
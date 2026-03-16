package srpm.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import srpm.dao.ISubmissionDAO;
import srpm.model.Submission;

@Repository
public class SubmissionDAO implements ISubmissionDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SubmissionDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createSubmission(Submission submission) {

        String sql = """
                INSERT INTO submission (file_url, note, student_id, project_id)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                submission.getFileUrl(),
                submission.getNote(),
                submission.getStudentId(),
                submission.getProjectId());
    }
}
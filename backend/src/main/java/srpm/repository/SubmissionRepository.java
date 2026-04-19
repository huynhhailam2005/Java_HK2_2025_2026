package srpm.repository;

import srpm.model.Submission;

import java.util.List;
import java.util.Optional;

/**
 * Repository wrapper contract for {@link Submission}.
 */
public interface SubmissionRepository {
	Optional<Submission> findById(Long id);

	Submission save(Submission submission);

	List<Submission> findByIssueId(Long issueId);
}


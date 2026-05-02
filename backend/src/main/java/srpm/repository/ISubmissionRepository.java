package srpm.repository;

import srpm.model.Submission;

import java.util.List;
import java.util.Optional;

public interface ISubmissionRepository {
	Optional<Submission> findById(Long id);

	Submission save(Submission submission);

	List<Submission> findByIssueId(Long issueId);

	List<Submission> findByGroupId(Long groupId);
}


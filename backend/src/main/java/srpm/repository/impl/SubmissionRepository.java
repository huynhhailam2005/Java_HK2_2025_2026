package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.SubmissionDao;
import srpm.model.Submission;
import srpm.repository.ISubmissionRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionRepository implements ISubmissionRepository {

	private final SubmissionDao submissionDao;

	public SubmissionRepository(SubmissionDao submissionDao) {
		this.submissionDao = submissionDao;
	}

	@Override
	public Optional<Submission> findById(Long id) {
		return submissionDao.findById(id);
	}

	@Override
	public Submission save(Submission submission) {
		return submissionDao.save(submission);
	}

	@Override
	public List<Submission> findByIssueId(Long issueId) {
		return submissionDao.findByIssueId(issueId);
	}

	@Override
	public List<Submission> findByGroupId(Long groupId) {
		return submissionDao.findByGroupId(groupId);
	}
}


package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.SubmissionDao;
import srpm.model.Submission;
import srpm.repository.SubmissionRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionRepositoryImpl implements SubmissionRepository {

	private final SubmissionDao submissionDao;

	public SubmissionRepositoryImpl(SubmissionDao submissionDao) {
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
}


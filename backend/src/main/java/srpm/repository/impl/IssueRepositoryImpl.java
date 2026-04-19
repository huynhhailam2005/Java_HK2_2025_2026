package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.IssueDao;
import srpm.model.Issue;
import srpm.repository.IssueRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class IssueRepositoryImpl implements IssueRepository {

	private final IssueDao issueDao;

	public IssueRepositoryImpl(IssueDao issueDao) {
		this.issueDao = issueDao;
	}

	@Override
	public Optional<Issue> findById(Long id) {
		return issueDao.findById(id);
	}

	@Override
	public Issue save(Issue issue) {
		return issueDao.save(issue);
	}

	@Override
	public Optional<Issue> findByIssueCodeAndGroupId(String issueCode, Long groupId) {
		return issueDao.findByIssueCodeAndGroupId(issueCode, groupId);
	}

	@Override
	public List<Issue> findByGroupIdOrderByCreatedAtDesc(Long groupId) {
		return issueDao.findByGroupIdOrderByCreatedAtDesc(groupId);
	}

	@Override
	public List<Issue> findByGroupIdAndIssueCodeIsNullAndIsDeletedFalse(Long groupId) {
		return issueDao.findByGroupIdAndIssueCodeIsNullAndIsDeletedFalse(groupId);
	}

	@Override
	public List<Issue> findIssuesNotInJiraKeys(Long groupId, List<String> jiraKeys) {
		return issueDao.findIssuesNotInJiraKeys(groupId, jiraKeys);
	}

	@Override
	public List<Issue> findByGroupIdAndIssueCodeIsNotNullAndIsDeletedFalse(Long groupId) {
		return issueDao.findByGroupIdAndIssueCodeIsNotNullAndIsDeletedFalse(groupId);
	}
}


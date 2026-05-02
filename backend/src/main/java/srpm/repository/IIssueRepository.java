package srpm.repository;

import srpm.model.Issue;

import java.util.List;
import java.util.Optional;

public interface IIssueRepository {

	Optional<Issue> findById(Long id);

	Issue save(Issue issue);

	Optional<Issue> findByIssueCodeAndGroupId(String issueCode, Long groupId);

	List<Issue> findByGroupIdOrderByCreatedAtDesc(Long groupId);

	List<Issue> findByGroupIdAndIssueCodeIsNull(Long groupId);

	List<Issue> findIssuesNotInJiraKeys(Long groupId, List<String> jiraKeys);

	List<Issue> findByGroupIdAndIssueCodeIsNotNull(Long groupId);

	List<Issue> findByAssignedToStudentId(Long studentId);
}

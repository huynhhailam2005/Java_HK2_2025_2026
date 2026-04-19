package srpm.repository;

import srpm.model.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Repository wrapper contract for {@link Issue}.
 */
public interface IssueRepository {

	Optional<Issue> findById(Long id);

	Issue save(Issue issue);

	Optional<Issue> findByIssueCodeAndGroupId(String issueCode, Long groupId);

	List<Issue> findByGroupIdOrderByCreatedAtDesc(Long groupId);

	List<Issue> findByGroupIdAndIssueCodeIsNullAndIsDeletedFalse(Long groupId);

	List<Issue> findIssuesNotInJiraKeys(Long groupId, List<String> jiraKeys);

	List<Issue> findByGroupIdAndIssueCodeIsNotNullAndIsDeletedFalse(Long groupId);
}




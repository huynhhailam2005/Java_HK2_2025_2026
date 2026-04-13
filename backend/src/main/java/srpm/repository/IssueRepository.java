package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import srpm.model.Issue;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    Optional<Issue> findByIssueCodeAndGroupId(String issueCode, Long groupId);
    List<Issue> findByGroupIdOrderByCreatedAtDesc(Long groupId);
    List<Issue> findByGroupIdAndIssueType(Long groupId, String issueType);

    List<Issue> findByGroupIdAndIssueCodeIsNullAndIsDeletedFalse(Long groupId);

    // Tìm các Issue của Group có issueCode nhưng không nằm trong danh sách các key từ Jira
    @Query("SELECT i FROM Issue i WHERE i.group.id = :groupId AND i.issueCode NOT IN :jiraKeys AND i.isDeleted = false")
    List<Issue> findIssuesNotInJiraKeys(@Param("groupId") Long groupId, @Param("jiraKeys") List<String> jiraKeys);

    List<Issue> findByGroupIdAndIssueCodeIsNotNullAndIsDeletedFalse(Long groupId);
}




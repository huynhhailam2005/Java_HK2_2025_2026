package srpm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import srpm.model.Issue;

import java.util.List;
import java.util.Optional;

@Repository
public class IssueDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<Issue> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Issue.class, id));
  }

  public Issue save(Issue issue) {
    if (issue.getId() == null) {
      entityManager.persist(issue);
      return issue;
    }
    return entityManager.merge(issue);
  }

  public Optional<Issue> findByIssueCodeAndGroupId(String issueCode, Long groupId) {
    List<Issue> result = entityManager
        .createQuery(
            "SELECT i FROM Issue i WHERE i.issueCode = :issueCode AND i.group.id = :groupId",
            Issue.class)
        .setParameter("issueCode", issueCode)
        .setParameter("groupId", groupId)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public List<Issue> findByGroupIdOrderByCreatedAtDesc(Long groupId) {
    return entityManager
        .createQuery(
            "SELECT i FROM Issue i WHERE i.group.id = :groupId ORDER BY i.createdAt DESC",
            Issue.class)
        .setParameter("groupId", groupId)
        .getResultList();
  }

  public List<Issue> findByGroupIdAndIssueCodeIsNullAndIsDeletedFalse(Long groupId) {
    return entityManager
        .createQuery(
            "SELECT i FROM Issue i WHERE i.group.id = :groupId AND i.issueCode IS NULL AND i.isDeleted = false",
            Issue.class)
        .setParameter("groupId", groupId)
        .getResultList();
  }

  public List<Issue> findIssuesNotInJiraKeys(Long groupId, List<String> jiraKeys) {
    if (jiraKeys == null || jiraKeys.isEmpty()) {
      return entityManager
          .createQuery(
              "SELECT i FROM Issue i WHERE i.group.id = :groupId AND i.issueCode IS NOT NULL AND i.isDeleted = false",
              Issue.class)
          .setParameter("groupId", groupId)
          .getResultList();
    }

    return entityManager
        .createQuery(
            "SELECT i FROM Issue i WHERE i.group.id = :groupId AND i.issueCode NOT IN :jiraKeys AND i.isDeleted = false",
            Issue.class)
        .setParameter("groupId", groupId)
        .setParameter("jiraKeys", jiraKeys)
        .getResultList();
  }

  public List<Issue> findByGroupIdAndIssueCodeIsNotNullAndIsDeletedFalse(Long groupId) {
    return entityManager
        .createQuery(
            "SELECT i FROM Issue i WHERE i.group.id = :groupId AND i.issueCode IS NOT NULL AND i.isDeleted = false",
            Issue.class)
        .setParameter("groupId", groupId)
        .getResultList();
  }
}





package srpm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import srpm.model.Submission;

import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<Submission> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Submission.class, id));
  }

  public Submission save(Submission submission) {
    if (submission.getId() == null) {
      entityManager.persist(submission);
      return submission;
    }
    return entityManager.merge(submission);
  }

  public List<Submission> findByIssueId(Long issueId) {
    return entityManager
        .createQuery("SELECT s FROM Submission s WHERE s.issue.id = :issueId", Submission.class)
        .setParameter("issueId", issueId)
        .getResultList();
  }

  public List<Submission> findByGroupId(Long groupId) {
    return entityManager
        .createQuery(
            "SELECT s FROM Submission s JOIN FETCH s.issue i JOIN FETCH s.submittedBy sm " +
            "WHERE i.group.id = :groupId ORDER BY s.submittedAt DESC", Submission.class)
        .setParameter("groupId", groupId)
        .getResultList();
  }
}



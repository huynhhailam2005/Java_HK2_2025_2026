package srpm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import srpm.model.Group;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupDao {
   @PersistenceContext
  private EntityManager entityManager;

  public Optional<Group> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Group.class, id));
  }

  public Optional<Group> findByIdWithStudentsAndLecturer(Long id) {
    List<Group> result = entityManager
        .createQuery(
            "SELECT g FROM Group g " +
                "LEFT JOIN FETCH g.groupMembers gm " +
                "LEFT JOIN FETCH gm.student " +
                "LEFT JOIN FETCH g.lecturer " +
                "WHERE g.id = :id",
            Group.class)
        .setParameter("id", id)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public List<Group> findAllWithStudentsAndLecturer() {
    return entityManager
        .createQuery(
            "SELECT DISTINCT g FROM Group g " +
                "LEFT JOIN FETCH g.groupMembers gm " +
                "LEFT JOIN FETCH gm.student " +
                "LEFT JOIN FETCH g.lecturer",
            Group.class)
        .getResultList();
  }

  public List<Group> findByLecturerId(Long lecturerId) {
    return entityManager
        .createQuery(
            "SELECT DISTINCT g FROM Group g " +
                "LEFT JOIN FETCH g.groupMembers gm " +
                "LEFT JOIN FETCH gm.student " +
                "LEFT JOIN FETCH g.lecturer " +
                "WHERE g.lecturer.id = :lecturerId",
            Group.class)
        .setParameter("lecturerId", lecturerId)
        .getResultList();
  }

  public boolean existsById(Long id) {
    Long count = entityManager
        .createQuery("SELECT COUNT(g) FROM Group g WHERE g.id = :id", Long.class)
        .setParameter("id", id)
        .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByGroupCode(String groupCode) {
    Long count = entityManager
        .createQuery("SELECT COUNT(g) FROM Group g WHERE g.groupCode = :groupCode", Long.class)
        .setParameter("groupCode", groupCode)
        .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByGroupName(String groupName) {
    Long count = entityManager
        .createQuery("SELECT COUNT(g) FROM Group g WHERE g.groupName = :groupName", Long.class)
        .setParameter("groupName", groupName)
        .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByGroupCodeAndIdNot(String groupCode, Long id) {
    Long count = entityManager
        .createQuery("SELECT COUNT(g) FROM Group g WHERE g.groupCode = :groupCode AND g.id <> :id", Long.class)
        .setParameter("groupCode", groupCode)
        .setParameter("id", id)
        .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByGroupNameAndIdNot(String groupName, Long id) {
    Long count = entityManager
        .createQuery("SELECT COUNT(g) FROM Group g WHERE g.groupName = :groupName AND g.id <> :id", Long.class)
        .setParameter("groupName", groupName)
        .setParameter("id", id)
        .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByJiraUrlAndProjectKey(String jiraUrl, String projectKey) {
    Long count = entityManager
        .createQuery(
            "SELECT COUNT(g) FROM Group g WHERE g.jiraUrl = :jiraUrl AND g.jiraProjectKey = :projectKey",
            Long.class)
        .setParameter("jiraUrl", jiraUrl)
        .setParameter("projectKey", projectKey)
        .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByJiraUrlAndProjectKeyAndIdNot(String jiraUrl, String projectKey, Long id) {
    Long count = entityManager
        .createQuery(
            "SELECT COUNT(g) FROM Group g WHERE g.jiraUrl = :jiraUrl AND g.jiraProjectKey = :projectKey AND g.id <> :id",
            Long.class)
        .setParameter("jiraUrl", jiraUrl)
        .setParameter("projectKey", projectKey)
        .setParameter("id", id)
        .getSingleResult();
    return count != null && count > 0;
  }

  public Group save(Group group) {
    if (group.getId() == null) {
      entityManager.persist(group);
      return group;
    }
    return entityManager.merge(group);
  }

  public void deleteById(Long id) {
    Group group = entityManager.find(Group.class, id);
    if (group != null) {
      entityManager.remove(group);
    }
  }
}


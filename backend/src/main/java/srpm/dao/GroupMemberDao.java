package srpm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupMemberDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<GroupMember> findById(Long id) {
    return Optional.ofNullable(entityManager.find(GroupMember.class, id));
  }

  public GroupMember save(GroupMember member) {
    if (member.getId() == null) {
      entityManager.persist(member);
      return member;
    }
    return entityManager.merge(member);
  }

  public void delete(GroupMember member) {
    GroupMember managed = member;
    if (!entityManager.contains(member)) {
      managed = entityManager.merge(member);
    }
    entityManager.remove(managed);
  }

  public Optional<GroupMember> findByGroupAndRole(Long groupId, GroupMemberRole role) {
    List<GroupMember> result = entityManager
        .createQuery(
            "SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.groupMemberRole = :role",
            GroupMember.class)
        .setParameter("groupId", groupId)
        .setParameter("role", role)
        .setMaxResults(1)
        .getResultList();

    return result.stream().findFirst();
  }

  public Optional<GroupMember> findByGroupAndStudent(Long groupId, Long studentId) {
    List<GroupMember> result = entityManager
        .createQuery(
            "SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.student.id = :studentId",
            GroupMember.class)
        .setParameter("groupId", groupId)
        .setParameter("studentId", studentId)
        .setMaxResults(1)
        .getResultList();

    return result.stream().findFirst();
  }

  public Optional<GroupMember> findByGroupAndJiraAccountId(Long groupId, String jiraAccountId) {
    List<GroupMember> result = entityManager
        .createQuery(
            "SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.student.jiraAccountId = :jiraAccountId",
            GroupMember.class)
        .setParameter("groupId", groupId)
        .setParameter("jiraAccountId", jiraAccountId)
        .setMaxResults(1)
        .getResultList();

    return result.stream().findFirst();
  }

  public boolean existsByGroupAndRole(Long groupId, GroupMemberRole role) {
    Long count = entityManager
        .createQuery(
            "SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.groupMemberRole = :role",
            Long.class)
        .setParameter("groupId", groupId)
        .setParameter("role", role)
        .getSingleResult();
    return count != null && count > 0;
  }

  public List<Group> findGroupsByStudentId(Long studentId) {
    return entityManager
        .createQuery(
            "SELECT DISTINCT g FROM Group g " +
                "LEFT JOIN FETCH g.groupMembers gm " +
                "LEFT JOIN FETCH gm.student " +
                "LEFT JOIN FETCH g.lecturer " +
                "WHERE g.id IN (" +
                "   SELECT gm2.group.id FROM GroupMember gm2 WHERE gm2.student.id = :studentId" +
                ")",
            Group.class)
        .setParameter("studentId", studentId)
        .getResultList();
  }

  public List<GroupMember> findByStudent(Long studentId) {
    return entityManager
        .createQuery("SELECT gm FROM GroupMember gm WHERE gm.student.id = :studentId", GroupMember.class)
        .setParameter("studentId", studentId)
        .getResultList();
  }

  public List<GroupMember> findByGroup(Long groupId) {
    return entityManager
        .createQuery("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId", GroupMember.class)
        .setParameter("groupId", groupId)
        .getResultList();
  }
}


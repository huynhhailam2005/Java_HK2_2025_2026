package srpm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import srpm.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<Student> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Student.class, id));
  }

  public List<Student> findAll() {
    return entityManager
        .createQuery("SELECT s FROM Student s ORDER BY s.studentCode", Student.class)
        .getResultList();
  }

  public Optional<Student> findByUserId(Long userId) {
    // Student PK = user_id
    return findById(userId);
  }

  public Optional<Student> findByJiraAccountId(String jiraAccountId) {
    List<Student> result = entityManager
        .createQuery("SELECT s FROM Student s WHERE s.jiraAccountId = :jiraAccountId", Student.class)
        .setParameter("jiraAccountId", jiraAccountId)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public Optional<Student> findByUsername(String username) {
    List<Student> result = entityManager
        .createQuery("SELECT s FROM Student s WHERE s.username = :username", Student.class)
        .setParameter("username", username)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public Optional<Student> findByEmail(String email) {
    List<Student> result = entityManager
        .createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class)
        .setParameter("email", email)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public boolean existsByStudentCode(String studentCode) {
    return count("SELECT COUNT(s) FROM Student s WHERE s.studentCode = :studentCode", "studentCode", studentCode) > 0;
  }

  public boolean existsByStudentCodeAndIdNot(String studentCode, Long id) {
    return entityManager
        .createQuery("SELECT COUNT(s) FROM Student s WHERE s.studentCode = :studentCode AND s.id <> :id", Long.class)
        .setParameter("studentCode", studentCode)
        .setParameter("id", id)
        .getSingleResult() > 0;
  }

  public boolean existsByUsername(String username) {
    return count("SELECT COUNT(s) FROM Student s WHERE s.username = :username", "username", username) > 0;
  }

  public boolean existsByEmail(String email) {
    return count("SELECT COUNT(s) FROM Student s WHERE s.email = :email", "email", email) > 0;
  }

  public boolean existsByUsernameAndIdNot(String username, Long id) {
    return entityManager
        .createQuery("SELECT COUNT(s) FROM Student s WHERE s.username = :username AND s.id <> :id", Long.class)
        .setParameter("username", username)
        .setParameter("id", id)
        .getSingleResult() > 0;
  }

  public boolean existsByEmailAndIdNot(String email, Long id) {
    return entityManager
        .createQuery("SELECT COUNT(s) FROM Student s WHERE s.email = :email AND s.id <> :id", Long.class)
        .setParameter("email", email)
        .setParameter("id", id)
        .getSingleResult() > 0;
  }

  public Student save(Student student) {
    if (student.getID() == null) {
      entityManager.persist(student);
      return student;
    }
    return entityManager.merge(student);
  }

  public List<Student> saveAll(List<Student> students) {
    if (students == null || students.isEmpty()) {
      return List.of();
    }

    List<Student> result = new ArrayList<>(students.size());
    for (Student student : students) {
      result.add(save(student));
    }

    entityManager.flush();
    return result;
  }

  private long count(String jpql, String paramName, Object paramValue) {
    Long count = entityManager
        .createQuery(jpql, Long.class)
        .setParameter(paramName, paramValue)
        .getSingleResult();
    return count != null ? count : 0L;
  }
}


package srpm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import srpm.model.Lecturer;

import java.util.Optional;

@Repository
public class LecturerDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<Lecturer> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Lecturer.class, id));
  }

  public Optional<Lecturer> findByUserId(Long userId) {
    // Lecturer PK = user_id
    return findById(userId);
  }

  public Lecturer save(Lecturer lecturer) {
    if (lecturer.getID() == null) {
      entityManager.persist(lecturer);
      return lecturer;
    }
    return entityManager.merge(lecturer);
  }

  public boolean existsByUsername(String username) {
    return count("SELECT COUNT(l) FROM Lecturer l WHERE l.username = :username", "username", username) > 0;
  }

  public boolean existsByEmail(String email) {
    return count("SELECT COUNT(l) FROM Lecturer l WHERE l.email = :email", "email", email) > 0;
  }

  public boolean existsByUsernameAndIdNot(String username, Long id) {
    return entityManager
        .createQuery("SELECT COUNT(l) FROM Lecturer l WHERE l.username = :username AND l.id <> :id", Long.class)
        .setParameter("username", username)
        .setParameter("id", id)
        .getSingleResult() > 0;
  }

  public boolean existsByEmailAndIdNot(String email, Long id) {
    return entityManager
        .createQuery("SELECT COUNT(l) FROM Lecturer l WHERE l.email = :email AND l.id <> :id", Long.class)
        .setParameter("email", email)
        .setParameter("id", id)
        .getSingleResult() > 0;
  }

  public boolean existsByLecturerCode(String lecturerCode) {
    return count("SELECT COUNT(l) FROM Lecturer l WHERE l.lecturerCode = :lecturerCode", "lecturerCode", lecturerCode) > 0;
  }

  public boolean existsByLecturerCodeAndIdNot(String lecturerCode, Long id) {
    return entityManager
        .createQuery("SELECT COUNT(l) FROM Lecturer l WHERE l.lecturerCode = :lecturerCode AND l.id <> :id", Long.class)
        .setParameter("lecturerCode", lecturerCode)
        .setParameter("id", id)
        .getSingleResult() > 0;
  }

  private long count(String jpql, String paramName, Object paramValue) {
    Long count = entityManager
        .createQuery(jpql, Long.class)
        .setParameter(paramName, paramValue)
        .getSingleResult();
    return count != null ? count : 0L;
  }
}


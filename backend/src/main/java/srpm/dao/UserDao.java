package srpm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import srpm.model.User;
import srpm.model.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<User> findById(Long id) {
    return Optional.ofNullable(entityManager.find(User.class, id));
  }

  public Optional<User> findByUsernameOrEmail(String username, String email) {
    List<User> result = entityManager
        .createQuery("SELECT u FROM User u WHERE u.username = :username OR u.email = :email", User.class)
        .setParameter("username", username)
        .setParameter("email", email)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public Optional<User> findByEmail(String email) {
    List<User> result = entityManager
        .createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
        .setParameter("email", email)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public Optional<User> findByUsername(String username) {
    List<User> result = entityManager
        .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
        .setParameter("username", username)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public boolean existsByUsername(String username) {
    return count("SELECT COUNT(u) FROM User u WHERE u.username = :username", "username", username) > 0;
  }

  public boolean existsByEmail(String email) {
    return count("SELECT COUNT(u) FROM User u WHERE u.email = :email", "email", email) > 0;
  }

  public boolean existsByUsernameAndIdNot(String username, Long id) {
    Long count = entityManager
        .createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.id <> :id", Long.class)
        .setParameter("username", username)
        .setParameter("id", id)
        .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByEmailAndIdNot(String email, Long id) {
    Long count = entityManager
        .createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email AND u.id <> :id", Long.class)
        .setParameter("email", email)
        .setParameter("id", id)
        .getSingleResult();
    return count != null && count > 0;
  }

  public List<User> findAllByUserRoleOrderByUsernameAsc(UserRole userRole) {
    return entityManager
        .createQuery("SELECT u FROM User u WHERE u.userRole = :userRole ORDER BY u.username ASC", User.class)
        .setParameter("userRole", userRole)
        .getResultList();
  }

  public List<User> findAllByUserRoleInOrderByUsernameAsc(List<UserRole> userRoles) {
    return entityManager
        .createQuery("SELECT u FROM User u WHERE u.userRole IN :userRoles ORDER BY u.username ASC", User.class)
        .setParameter("userRoles", userRoles)
        .getResultList();
  }

  public Optional<User> findByIdAndUserRoleIn(Long id, List<UserRole> userRoles) {
    List<User> result = entityManager
        .createQuery("SELECT u FROM User u WHERE u.id = :id AND u.userRole IN :userRoles", User.class)
        .setParameter("id", id)
        .setParameter("userRoles", userRoles)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }

  public User save(User user) {
    if (user.getID() == null) {
      entityManager.persist(user);
      return user;
    }
    return entityManager.merge(user);
  }

  public void deleteById(Long id) {
    User user = entityManager.find(User.class, id);
    if (user != null) {
      entityManager.remove(user);
    }
  }

  private long count(String jpql, String paramName, Object paramValue) {
    Long count = entityManager
        .createQuery(jpql, Long.class)
        .setParameter(paramName, paramValue)
        .getSingleResult();
    return count != null ? count : 0L;
  }
}


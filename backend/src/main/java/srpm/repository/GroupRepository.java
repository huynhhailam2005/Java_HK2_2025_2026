package srpm.repository;

import srpm.model.Group;

import java.util.List;
import java.util.Optional;

/**
 * Repository wrapper contract for {@link Group}.
 * <p>
 * This layer wraps DAO access and is the default dependency for services.
 */
public interface GroupRepository {

	Optional<Group> findById(Long id);

	Optional<Group> findByIdWithStudentsAndLecturer(Long id);

	List<Group> findAllWithStudentsAndLecturer();

	List<Group> findByLecturerId(Long lecturerId);

	boolean existsById(Long id);

	boolean existsByGroupCode(String groupCode);

	boolean existsByGroupName(String groupName);

	boolean existsByGroupCodeAndIdNot(String groupCode, Long id);

	boolean existsByGroupNameAndIdNot(String groupName, Long id);

	boolean existsByJiraUrlAndProjectKey(String jiraUrl, String projectKey);

	boolean existsByJiraUrlAndProjectKeyAndIdNot(String jiraUrl, String projectKey, Long id);

	Group save(Group group);

	void deleteById(Long id);
}

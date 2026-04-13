package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import srpm.model.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.groupMembers gm LEFT JOIN FETCH gm.student LEFT JOIN FETCH g.lecturer")
    List<Group> findAllWithStudentsAndLecturer();

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.groupMembers gm LEFT JOIN FETCH gm.student LEFT JOIN FETCH g.lecturer WHERE g.id = ?1")
    Optional<Group> findByIdWithStudentsAndLecturer(Long id);

    boolean existsByGroupCode(String groupCode);

    boolean existsByGroupName(String groupName);

    boolean existsByGroupCodeAndIdNot(String groupCode, Long id);

    boolean existsByGroupNameAndIdNot(String groupName, Long id);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Group g WHERE g.jiraUrl = :jiraUrl AND g.jiraProjectKey = :projectKey")
    boolean existsByJiraUrlAndProjectKey(@Param("jiraUrl") String jiraUrl, @Param("projectKey") String projectKey);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Group g WHERE g.jiraUrl = :jiraUrl AND g.jiraProjectKey = :projectKey AND g.id != :id")
    boolean existsByJiraUrlAndProjectKeyAndIdNot(@Param("jiraUrl") String jiraUrl, @Param("projectKey") String projectKey, @Param("id") Long id);

    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.groupMembers gm LEFT JOIN FETCH gm.student LEFT JOIN FETCH g.lecturer WHERE g.lecturer.id = :lecturerId")
    List<Group> findByLecturerId(@Param("lecturerId") Long lecturerId);
}

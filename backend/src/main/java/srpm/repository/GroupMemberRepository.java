package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;

import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.groupMemberRole = :role")
    Optional<GroupMember> findByGroupAndRole(@Param("groupId") Long groupId, @Param("role") GroupMemberRole role);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.student.id = :studentId")
    Optional<GroupMember> findByGroupAndStudent(@Param("groupId") Long groupId, @Param("studentId") Long studentId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.student.jiraAccountId = :jiraAccountId")
    Optional<GroupMember> findByGroupAndJiraAccountId(@Param("groupId") Long groupId, @Param("jiraAccountId") String jiraAccountId);

    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.groupMemberRole = :role")
    boolean existsByGroupAndRole(@Param("groupId") Long groupId, @Param("role") GroupMemberRole role);

    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.groupMembers gm LEFT JOIN FETCH gm.student LEFT JOIN FETCH g.lecturer WHERE g.id IN (SELECT gm2.group.id FROM GroupMember gm2 WHERE gm2.student.id = :studentId)")
    java.util.List<srpm.model.Group> findGroupsByStudentId(@Param("studentId") Long studentId);

    // Lấy tất cả GroupMembers của một Student
    @Query("SELECT gm FROM GroupMember gm WHERE gm.student.id = :studentId")
    java.util.List<GroupMember> findByStudent(@Param("studentId") Long studentId);
}

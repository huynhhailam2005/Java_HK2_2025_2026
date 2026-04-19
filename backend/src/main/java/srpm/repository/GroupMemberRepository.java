package srpm.repository;

import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository wrapper contract for {@link GroupMember}.
 */
public interface GroupMemberRepository {

	Optional<GroupMember> findById(Long id);

	GroupMember save(GroupMember member);

	void delete(GroupMember member);

	Optional<GroupMember> findByGroupAndRole(Long groupId, GroupMemberRole role);

	Optional<GroupMember> findByGroupAndStudent(Long groupId, Long studentId);

	Optional<GroupMember> findByGroupAndJiraAccountId(Long groupId, String jiraAccountId);

	boolean existsByGroupAndRole(Long groupId, GroupMemberRole role);

	List<Group> findGroupsByStudentId(Long studentId);

	List<GroupMember> findByStudent(Long studentId);

	List<GroupMember> findByGroup(Long groupId);
}

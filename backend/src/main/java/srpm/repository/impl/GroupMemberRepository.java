package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.GroupMemberDao;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;
import srpm.repository.IGroupMemberRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupMemberRepository implements IGroupMemberRepository {

	private final GroupMemberDao groupMemberDao;

	public GroupMemberRepository(GroupMemberDao groupMemberDao) {
		this.groupMemberDao = groupMemberDao;
	}

	@Override
	public Optional<GroupMember> findById(Long id) {
		return groupMemberDao.findById(id);
	}

	@Override
	public GroupMember save(GroupMember member) {
		return groupMemberDao.save(member);
	}

	@Override
	public void delete(GroupMember member) {
		groupMemberDao.delete(member);
	}

	@Override
	public Optional<GroupMember> findByGroupAndRole(Long groupId, GroupMemberRole role) {
		return groupMemberDao.findByGroupAndRole(groupId, role);
	}

	@Override
	public Optional<GroupMember> findByGroupAndStudent(Long groupId, Long studentId) {
		return groupMemberDao.findByGroupAndStudent(groupId, studentId);
	}

	@Override
	public Optional<GroupMember> findByGroupAndJiraAccountId(Long groupId, String jiraAccountId) {
		return groupMemberDao.findByGroupAndJiraAccountId(groupId, jiraAccountId);
	}

	@Override
	public boolean existsByGroupAndRole(Long groupId, GroupMemberRole role) {
		return groupMemberDao.existsByGroupAndRole(groupId, role);
	}

	@Override
	public List<Group> findGroupsByStudentId(Long studentId) {
		return groupMemberDao.findGroupsByStudentId(studentId);
	}

	@Override
	public List<GroupMember> findByStudent(Long studentId) {
		return groupMemberDao.findByStudent(studentId);
	}

	@Override
	public List<GroupMember> findByGroup(Long groupId) {
		return groupMemberDao.findByGroup(groupId);
	}
}


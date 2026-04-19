package srpm.repository.impl;

import org.springframework.stereotype.Repository;
import srpm.dao.GroupDao;
import srpm.model.Group;
import srpm.repository.GroupRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupRepositoryImpl implements GroupRepository {

	private final GroupDao groupDao;

	public GroupRepositoryImpl(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	@Override
	public Optional<Group> findById(Long id) {
		return groupDao.findById(id);
	}

	@Override
	public Optional<Group> findByIdWithStudentsAndLecturer(Long id) {
		return groupDao.findByIdWithStudentsAndLecturer(id);
	}

	@Override
	public List<Group> findAllWithStudentsAndLecturer() {
		return groupDao.findAllWithStudentsAndLecturer();
	}

	@Override
	public List<Group> findByLecturerId(Long lecturerId) {
		return groupDao.findByLecturerId(lecturerId);
	}

	@Override
	public boolean existsById(Long id) {
		return groupDao.existsById(id);
	}

	@Override
	public boolean existsByGroupCode(String groupCode) {
		return groupDao.existsByGroupCode(groupCode);
	}

	@Override
	public boolean existsByGroupName(String groupName) {
		return groupDao.existsByGroupName(groupName);
	}

	@Override
	public boolean existsByGroupCodeAndIdNot(String groupCode, Long id) {
		return groupDao.existsByGroupCodeAndIdNot(groupCode, id);
	}

	@Override
	public boolean existsByGroupNameAndIdNot(String groupName, Long id) {
		return groupDao.existsByGroupNameAndIdNot(groupName, id);
	}

	@Override
	public boolean existsByJiraUrlAndProjectKey(String jiraUrl, String projectKey) {
		return groupDao.existsByJiraUrlAndProjectKey(jiraUrl, projectKey);
	}

	@Override
	public boolean existsByJiraUrlAndProjectKeyAndIdNot(String jiraUrl, String projectKey, Long id) {
		return groupDao.existsByJiraUrlAndProjectKeyAndIdNot(jiraUrl, projectKey, id);
	}

	@Override
	public Group save(Group group) {
		return groupDao.save(group);
	}

	@Override
	public void deleteById(Long id) {
		groupDao.deleteById(id);
	}
}


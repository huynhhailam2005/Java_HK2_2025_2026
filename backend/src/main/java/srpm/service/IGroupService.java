package srpm.service;

import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.model.Group;

import java.util.List;
import java.util.Optional;

public interface IGroupService {

    List<Group> getAllGroups();

    Optional<Group> getGroupById(Long id);

    Group createGroup(GroupRequest req);

    Optional<Group> updateGroupName(Long id, String newName);

    Optional<Group> updateGroupLecturer(Long id, Long lecturerId);

    Optional<Group> updateGroupInfo(Long id, UpdateGroupRequest request);

    boolean deleteGroup(Long id);

    Group addStudent(Long groupId, Long studentId);

    boolean removeStudent(Long groupId, Long studentId);

    List<Group> getGroupsByLecturer(Long lecturerId);

    List<Group> getGroupsByStudent(Long studentId);
}


package srpm.service;

import srpm.dto.request.GroupRequest;
import srpm.dto.request.UpdateGroupRequest;
import srpm.dto.GroupDto;

import java.util.List;
import java.util.Optional;

public interface IGroupService {

    List<GroupDto> getAllGroups();

    Optional<GroupDto> getGroupById(Long id);

    GroupDto createGroup(GroupRequest req);

    Optional<GroupDto> updateGroupName(Long id, String newName);

    Optional<GroupDto> updateGroupLecturer(Long id, Long lecturerId);

    Optional<GroupDto> updateGroupInfo(Long id, UpdateGroupRequest request);

    boolean deleteGroup(Long id);

    GroupDto addStudent(Long groupId, Long studentId);

    boolean removeStudent(Long groupId, Long studentId);

    boolean removeMember(Long groupId, Long memberId);

    List<GroupDto> getGroupsByLecturer(Long lecturerId);

    List<GroupDto> getGroupsByStudent(Long studentId);
}




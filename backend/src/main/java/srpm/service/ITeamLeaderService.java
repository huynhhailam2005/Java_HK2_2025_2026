package srpm.service;

import srpm.model.GroupMember;

import java.util.Optional;

public interface ITeamLeaderService {

    GroupMember assignTeamLeader(Long groupId, Long studentId);

    GroupMember changeTeamLeader(Long groupId, Long newStudentId);

    Optional<GroupMember> getTeamLeader(Long groupId);

    boolean removeTeamLeader(Long groupId);
}


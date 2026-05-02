package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;
import srpm.model.Student;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IGroupRepository;
import srpm.repository.IStudentRepository;
import srpm.service.ITeamLeaderService;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TeamLeaderService implements ITeamLeaderService {

    private final IGroupRepository IGroupRepository;
    private final IStudentRepository IStudentRepository;
    private final IGroupMemberRepository IGroupMemberRepository;

    @Autowired
    public TeamLeaderService(IGroupRepository IGroupRepository, IStudentRepository IStudentRepository,
                             IGroupMemberRepository IGroupMemberRepository) {
        this.IGroupRepository = IGroupRepository;
        this.IStudentRepository = IStudentRepository;
        this.IGroupMemberRepository = IGroupMemberRepository;
    }

    @Transactional
    public GroupMember assignTeamLeader(Long groupId, Long studentId) {
        Group group = IGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        Student student = IStudentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + studentId));

        if (!group.getGroupMembers().stream().anyMatch(gm -> gm.getStudent().getId().equals(studentId))) {
            throw new RuntimeException("Sinh viên không phải thành viên của nhóm này");
        }

        if (IGroupMemberRepository.existsByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER)) {
            throw new RuntimeException("Nhóm đã có nhóm trưởng rồi. Hãy đổi nhóm trưởng nếu cần");
        }

        Optional<GroupMember> existingMember = IGroupMemberRepository.findByGroupAndStudent(groupId, studentId);
        GroupMember member;

        if (existingMember.isPresent()) {
            member = existingMember.get();
            member.setGroupMemberRole(GroupMemberRole.TEAM_LEADER);
        } else {
            member = new GroupMember(group, student, GroupMemberRole.TEAM_LEADER);
        }

        return IGroupMemberRepository.save(member);
    }

    @Transactional
    public GroupMember changeTeamLeader(Long groupId, Long newStudentId) {
        Group group = IGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        Student newStudent = IStudentRepository.findById(newStudentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + newStudentId));

        if (!group.getGroupMembers().stream().anyMatch(gm -> gm.getStudent().getId().equals(newStudentId))) {
            throw new RuntimeException("Sinh viên không phải thành viên của nhóm này");
        }

        Optional<GroupMember> currentTeamLeader = IGroupMemberRepository.findByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER);

        if (currentTeamLeader.isPresent()) {
            GroupMember oldLeader = currentTeamLeader.get();
            oldLeader.setGroupMemberRole(GroupMemberRole.TEAM_MEMBER);
            IGroupMemberRepository.save(oldLeader);
        }

        Optional<GroupMember> newLeaderMember = IGroupMemberRepository.findByGroupAndStudent(groupId, newStudentId);
        GroupMember member;

        if (newLeaderMember.isPresent()) {
            member = newLeaderMember.get();
            member.setGroupMemberRole(GroupMemberRole.TEAM_LEADER);
        } else {
            member = new GroupMember(group, newStudent, GroupMemberRole.TEAM_LEADER);
        }

        return IGroupMemberRepository.save(member);
    }

    public Optional<GroupMember> getTeamLeader(Long groupId) {
        return IGroupMemberRepository.findByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER);
    }

    @Transactional
    public boolean removeTeamLeader(Long groupId) {
        Optional<GroupMember> teamLeader = IGroupMemberRepository.findByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER);

        if (teamLeader.isPresent()) {
            GroupMember member = teamLeader.get();
            member.setGroupMemberRole(GroupMemberRole.TEAM_MEMBER);
            IGroupMemberRepository.save(member);
            return true;
        }
        return false;
    }
}


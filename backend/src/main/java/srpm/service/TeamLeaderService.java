package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.GroupMemberRole;
import srpm.model.Student;
import srpm.repository.GroupMemberRepository;
import srpm.repository.GroupRepository;
import srpm.repository.StudentRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TeamLeaderService {

    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Autowired
    public TeamLeaderService(GroupRepository groupRepository, StudentRepository studentRepository,
                             GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public GroupMember assignTeamLeader(Long groupId, Long studentId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + studentId));

        if (!group.getGroupMembers().stream().anyMatch(gm -> gm.getStudent().getId().equals(studentId))) {
            throw new RuntimeException("Sinh viên không phải thành viên của nhóm này");
        }

        if (groupMemberRepository.existsByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER)) {
            throw new RuntimeException("Nhóm đã có nhóm trưởng rồi. Hãy đổi nhóm trưởng nếu cần");
        }

        Optional<GroupMember> existingMember = groupMemberRepository.findByGroupAndStudent(groupId, studentId);
        GroupMember member;

        if (existingMember.isPresent()) {
            member = existingMember.get();
            member.setGroupMemberRole(GroupMemberRole.TEAM_LEADER);
        } else {
            member = new GroupMember(group, student, GroupMemberRole.TEAM_LEADER);
        }

        return groupMemberRepository.save(member);
    }

    @Transactional
    public GroupMember changeTeamLeader(Long groupId, Long newStudentId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        Student newStudent = studentRepository.findById(newStudentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + newStudentId));

        if (!group.getGroupMembers().stream().anyMatch(gm -> gm.getStudent().getId().equals(newStudentId))) {
            throw new RuntimeException("Sinh viên không phải thành viên của nhóm này");
        }

        Optional<GroupMember> currentTeamLeader = groupMemberRepository.findByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER);

        if (currentTeamLeader.isPresent()) {
            GroupMember oldLeader = currentTeamLeader.get();
            oldLeader.setGroupMemberRole(GroupMemberRole.TEAM_MEMBER);
            groupMemberRepository.save(oldLeader);
        }

        Optional<GroupMember> newLeaderMember = groupMemberRepository.findByGroupAndStudent(groupId, newStudentId);
        GroupMember member;

        if (newLeaderMember.isPresent()) {
            member = newLeaderMember.get();
            member.setGroupMemberRole(GroupMemberRole.TEAM_LEADER);
        } else {
            member = new GroupMember(group, newStudent, GroupMemberRole.TEAM_LEADER);
        }

        return groupMemberRepository.save(member);
    }

    public Optional<GroupMember> getTeamLeader(Long groupId) {
        return groupMemberRepository.findByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER);
    }

    @Transactional
    public boolean removeTeamLeader(Long groupId) {
        Optional<GroupMember> teamLeader = groupMemberRepository.findByGroupAndRole(groupId, GroupMemberRole.TEAM_LEADER);

        if (teamLeader.isPresent()) {
            GroupMember member = teamLeader.get();
            member.setGroupMemberRole(GroupMemberRole.TEAM_MEMBER);
            groupMemberRepository.save(member);
            return true;
        }
        return false;
    }
}


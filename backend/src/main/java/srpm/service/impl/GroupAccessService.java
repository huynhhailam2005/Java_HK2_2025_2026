package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import srpm.model.*;
import srpm.repository.IGroupRepository;
import srpm.repository.IUserRepository;
import srpm.service.IGroupAccessService;

@Service
public class GroupAccessService implements IGroupAccessService {

    private final IGroupRepository groupDao;
    private final IUserRepository userDao;

    @Autowired
    public GroupAccessService(IGroupRepository groupDao, IUserRepository userDao) {
        this.groupDao = groupDao;
        this.userDao = userDao;
    }

    public boolean canAccessGroup(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        var userOptional = userDao.findByUsernameOrEmail(username, username);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        var groupOptional = groupDao.findById(groupId);

        if (groupOptional.isEmpty()) {
            return false;
        }

        Group group = groupOptional.get();

        if (user instanceof Admin) {
            return true;
        }

        if (user instanceof Lecturer) {
            Lecturer lecturer = (Lecturer) user;
            return group.getLecturer().getId().equals(lecturer.getId());
        }

        if (user instanceof Student) {
            Student student = (Student) user;
            return group.getGroupMembers().stream().anyMatch(gm -> gm.getStudent().getId().equals(student.getId()));
        }

        return false;
    }

    public boolean isTeamLeaderOfGroup(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        var userOptional = userDao.findByUsernameOrEmail(username, username);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        var groupOptional = groupDao.findById(groupId);

        if (groupOptional.isEmpty()) {
            return false;
        }

        Group group = groupOptional.get();

        if (user instanceof Admin) {
            return true;
        }

        if (user instanceof Lecturer) {
            Lecturer lecturer = (Lecturer) user;
            return group.getLecturer().getId().equals(lecturer.getId());
        }

        if (user instanceof Student) {
            Student student = (Student) user;
            return group.getGroupMembers().stream()
                    .anyMatch(gm -> gm.getStudent().getId().equals(student.getId()) &&
                            gm.getGroupMemberRole() == GroupMemberRole.TEAM_LEADER);
        }

        return false;
    }
}

package srpm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import srpm.model.*;
import srpm.repository.GroupRepository;
import srpm.repository.UserRepository;
import srpm.service.IGroupAccessService;

@Service
public class GroupAccessService implements IGroupAccessService {

    private final GroupRepository groupDao;
    private final UserRepository userDao;

    @Autowired
    public GroupAccessService(GroupRepository groupDao, UserRepository userDao) {
        this.groupDao = groupDao;
        this.userDao = userDao;
    }

    /**
     * Kiểm tra xem user hiện tại có quyền truy cập group hay không
     * - Admin: có thể truy cập tất cả groups
     * - Lecturer: có thể truy cập group mà họ dạy
     * - Student: có thể truy cập group mà họ là thành viên
     */
    public boolean canAccessGroup(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // authentication.getName() trả về username từ JWT
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

        // Admin có thể truy cập tất cả
        if (user instanceof Admin) {
            return true;
        }

        // Lecturer có thể truy cập group mà họ dạy
        if (user instanceof Lecturer) {
            Lecturer lecturer = (Lecturer) user;
            return group.getLecturer().getId().equals(lecturer.getId());
        }

        // Student có thể truy cập group mà họ là thành viên
        if (user instanceof Student) {
            Student student = (Student) user;
            return group.getGroupMembers().stream().anyMatch(gm -> gm.getStudent().getId().equals(student.getId()));
        }

        return false;
    }

    /**
     * Kiểm tra xem user hiện tại có phải Team Leader của group hay không
     * - Admin: được phép
     * - Student có role TEAM_LEADER trong group: được phép
     * - Lecturer quản lý group: được phép
     */
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

        // Admin được phép
        if (user instanceof Admin) {
            return true;
        }

        // Lecturer quản lý group được phép
        if (user instanceof Lecturer) {
            Lecturer lecturer = (Lecturer) user;
            return group.getLecturer().getId().equals(lecturer.getId());
        }

        // Student phải là TEAM_LEADER trong group
        if (user instanceof Student) {
            Student student = (Student) user;
            return group.getGroupMembers().stream()
                    .anyMatch(gm -> gm.getStudent().getId().equals(student.getId()) &&
                            gm.getGroupMemberRole() == GroupMemberRole.TEAM_LEADER);
        }

        return false;
    }
}


package srpm.service;

public interface IGroupAccessService {

    boolean canAccessGroup(Long groupId);

    boolean isTeamLeaderOfGroup(Long groupId);
}


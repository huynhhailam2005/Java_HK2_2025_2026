package srpm.service;

import srpm.dto.JiraGroupDto;
import srpm.dto.JiraUserDto;
import srpm.model.Group;

import java.util.List;

public interface IJiraGroupSyncService {
    JiraGroupDto syncJiraGroupToLocalGroup(Long groupId, String jiraGroupName);

    void syncGroupMembers(Group group, List<JiraUserDto> jiraMembers);
}


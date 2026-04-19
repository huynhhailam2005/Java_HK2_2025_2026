package srpm.service;

import srpm.dto.response.JiraGroupDto;

import java.util.List;

public interface IJiraGroupSyncService {

    JiraGroupDto syncJiraGroupToLocalGroup(Long groupId, String jiraGroupName);

    List<String> getJiraGroups(Long groupId);
}


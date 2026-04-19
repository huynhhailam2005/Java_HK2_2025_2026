package srpm.service;

import java.util.Map;

public interface IJiraIssueSyncService {

    Map<String, Object> syncJiraIssuesToLocalIssues(Long groupId, String projectKey);
}


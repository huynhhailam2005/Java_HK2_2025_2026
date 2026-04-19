package srpm.service;

import srpm.model.Issue;

public interface IJiraIssuePushService {

    Issue createIssueOnJira(
            Issue issue,
            String jiraUrl,
            String projectKey,
            String jiraAdminEmail,
            String jiraApiToken
    );

    Issue updateIssueOnJira(
            Issue issue,
            String jiraUrl,
            String jiraAdminEmail,
            String jiraApiToken
    );

    void deleteIssueOnJira(
            Issue issue,
            String jiraUrl,
            String jiraAdminEmail,
            String jiraApiToken
    );

    void updateJiraStatus(
            String issueCode,
            String targetStatusName,
            String jiraUrl,
            String jiraAdminEmail,
            String jiraApiToken
    );
}


package srpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import srpm.model.Group;
import srpm.model.Issue;
import srpm.model.IssueType;
import srpm.model.IssueStatus;
import srpm.model.SyncStatus;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IGroupRepository;
import srpm.repository.IIssueRepository;
import srpm.service.IJiraIssueSyncService;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class JiraIssueSyncService implements IJiraIssueSyncService {

    private final IGroupRepository IGroupRepository;
    private final IIssueRepository IIssueRepository;
    private final IGroupMemberRepository IGroupMemberRepository;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(JiraIssueSyncService.class);

    @Autowired
    public JiraIssueSyncService(
            IGroupRepository IGroupRepository,
            IIssueRepository IIssueRepository,
            IGroupMemberRepository IGroupMemberRepository,
            RestTemplate restTemplate
    ) {
        this.IGroupRepository = IGroupRepository;
        this.IIssueRepository = IIssueRepository;
        this.IGroupMemberRepository = IGroupMemberRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Map<String, Object> syncJiraIssuesToLocalIssues(Long groupId, String projectKey) {
        Group group = IGroupRepository.findByIdWithStudentsAndLecturer(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group: " + groupId));

        if (group.getJiraUrl() == null || group.getJiraUrl().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình URL Jira cho group này");
        }
        if (group.getJiraApiToken() == null || group.getJiraApiToken().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình API token Jira cho group này");
        }
        if (group.getJiraAdminEmail() == null || group.getJiraAdminEmail().isEmpty()) {
            throw new RuntimeException("Chưa cấu hình email admin Jira cho group này");
        }

        logger.info("Bắt đầu đồng bộ issues từ Jira project: {}", projectKey);

        List<Map<String, Object>> jiraIssues = fetchAllIssuesFromJira(
                group.getJiraUrl(),
                projectKey,
                group.getJiraAdminEmail(),
                group.getJiraApiToken()
        );

        Map<String, Issue> syncedIssuesMap = syncIssuesInOrder(group, jiraIssues);

        return Map.of(
                "success", true,
                "totalIssues", syncedIssuesMap.size(),
                "message", "Đã đồng bộ thành công " + syncedIssuesMap.size() + " issues từ Jira"
        );
    }

    private List<Map<String, Object>> fetchAllIssuesFromJira(String jiraUrl, String projectKey, String adminEmail, String apiToken) {
        try {
            String urlBase = jiraUrl.replaceAll("/$", "") + "/rest/api/3/search/jql";

            String jql = "project = \"" + projectKey + "\"";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jql", jql);
            requestBody.put("maxResults", 1000);
            requestBody.put("fields", new String[]{"*all"});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = adminEmail + ":" + apiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    urlBase,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> issues = (List<Map<String, Object>>) response.getBody().get("issues");
                logger.info("Đã hút về {} issues từ Jira API", issues != null ? issues.size() : 0);
                return issues != null ? issues : new ArrayList<>();
            } else {
                throw new RuntimeException("Lỗi HTTP " + response.getStatusCode().value() + " khi gọi Jira");
            }
        } catch (Exception e) {
            logger.error("Lỗi khi fetch issues từ Jira: " + e.getMessage());
            throw new RuntimeException("Lỗi đồng bộ Jira: " + e.getMessage());
        }
    }

    @Transactional
    protected Map<String, Issue> syncIssuesInOrder(Group group, List<Map<String, Object>> jiraIssues) {
        Map<String, Issue> syncedIssuesMap = new HashMap<>();
        Map<String, IssueType> localTypeMap = new HashMap<>();

        List<String> jiraKeys = new ArrayList<>();

        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            jiraKeys.add(key);
            localTypeMap.put(key, determineIssueType(jiraIssue));
        }

        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            if (localTypeMap.get(key) == IssueType.EPIC) {
                Issue localIssue = syncSingleIssue(group, jiraIssue, null, IssueType.EPIC);
                syncedIssuesMap.put(key, localIssue);
            }
        }

        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            IssueType type = localTypeMap.get(key);
            if (type == IssueType.STORY || type == IssueType.TASK || type == IssueType.BUG) {
                String parentKey = getParentKey(jiraIssue);
                Issue parentIssue = (parentKey != null) ? syncedIssuesMap.get(parentKey) : null;

                Issue localIssue = syncSingleIssue(group, jiraIssue, parentIssue, type);
                syncedIssuesMap.put(key, localIssue);
            }
        }

        for (Map<String, Object> jiraIssue : jiraIssues) {
            String key = (String) jiraIssue.get("key");
            if (localTypeMap.get(key) == IssueType.SUB_TASK) {
                String parentKey = getParentKey(jiraIssue);
                Issue parentIssue = (parentKey != null) ? syncedIssuesMap.get(parentKey) : null;

                if (parentIssue != null) {
                    Issue localIssue = syncSingleIssue(group, jiraIssue, parentIssue, IssueType.SUB_TASK);
                    syncedIssuesMap.put(key, localIssue);
                } else {
                    logger.warn("Bỏ qua Sub-task {} vì không tìm thấy issue cha {}", key, parentKey);
                }
            }
        }

        logger.info("Kiểm tra các Issue bị xóa từ phía Jira...");
        List<Issue> deletedOnJira = IIssueRepository.findIssuesNotInJiraKeys(group.getId(), jiraKeys);
        for (Issue deletedIssue : deletedOnJira) {
            logger.warn("Phát hiện Issue {} bị xóa từ phía Jira, đánh dấu Soft-Delete trên SRPM", deletedIssue.getIssueCode());
            deletedIssue.setSyncStatus(SyncStatus.SYNCED);
            IIssueRepository.save(deletedIssue);
        }

        return syncedIssuesMap;
    }

    @Transactional
    protected Issue syncSingleIssue(Group group, Map<String, Object> jiraIssue, Issue parentIssue, IssueType issueType) {
        String issueKey = (String) jiraIssue.get("key");
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) jiraIssue.get("fields");

        Optional<Issue> existing = IIssueRepository.findByIssueCodeAndGroupId(issueKey, group.getId());
        Issue localIssue = existing.orElse(new Issue());

        localIssue.setIssueCode(issueKey);
        localIssue.setGroup(group);
        localIssue.setTitle((String) fields.get("summary"));
        localIssue.setDescription(extractDescriptionFromJiraFields(fields));
        localIssue.setIssueType(issueType);

        if (parentIssue != null) {
            localIssue.setParent(parentIssue);
        } else {
            localIssue.setParent(null);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> assignee = (Map<String, Object>) fields.get("assignee");
        if (assignee != null) {
            String accountId = (String) assignee.get("accountId");
            String emailAddress = (String) assignee.get("emailAddress");
            String displayName = (String) assignee.get("displayName");

            logger.info("Issue {} has assignee - accountId: {}, email: {}, displayName: {}", issueKey, accountId, emailAddress, displayName);

            if (accountId != null) {
                logger.info("Tìm GroupMember trong group {} có jiraAccountId: {}", group.getId(), accountId);
                        var memberOpt = IGroupMemberRepository.findByGroupAndJiraAccountId(group.getId(), accountId);

                if (memberOpt.isPresent()) {
                    localIssue.setAssignedTo(memberOpt.get());
                    logger.info("Assigned issue {} to GroupMember {} (jiraAccountId: {})",
                        issueKey, memberOpt.get().getId(), accountId);
                } else {
                    logger.warn("Không tìm thấy GroupMember trong group {} với jiraAccountId: {}",
                        group.getId(), accountId);
                }
            }
        } else {
            logger.debug("Issue {} không có assignee", issueKey);
            localIssue.setAssignedTo(null);
        }

        if (existing.isEmpty()) {
            localIssue.setStatus(IssueStatus.TODO);
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) fields.get("status");
            if (status != null) {
                String jiraStatusName = (String) status.get("name");
                IssueStatus mappedStatus = mapJiraStatusToLocal(jiraStatusName);
                localIssue.setStatus(mappedStatus);
                logger.info("✓ Đồng bộ status từ Jira: {} -> {}", jiraStatusName, mappedStatus);
            }
        }

        localIssue.setSyncStatus(SyncStatus.SYNCED);

        return IIssueRepository.save(localIssue);
    }

    private String getParentKey(Map<String, Object> jiraIssue) {
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) jiraIssue.get("fields");
        if (fields == null) return null;

        @SuppressWarnings("unchecked")
        Map<String, Object> parent = (Map<String, Object>) fields.get("parent");
        if (parent != null) {
            return (String) parent.get("key");
        }

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (entry.getKey().startsWith("customfield_") && entry.getValue() instanceof String val) {
                if (val.matches("^[A-Z][A-Z0-9]+-[0-9]+$")) {
                    return val;
                }
            }
        }
        return null;
    }

    private IssueType determineIssueType(Map<String, Object> jiraIssue) {
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) jiraIssue.get("fields");
        if (fields == null) return IssueType.TASK;

        @SuppressWarnings("unchecked")
        Map<String, Object> it = (Map<String, Object>) fields.get("issuetype");
        if (it == null) return IssueType.TASK;

        if (Boolean.TRUE.equals(it.get("subtask"))) {
            return IssueType.SUB_TASK;
        }

        Number level = (Number) it.get("hierarchyLevel");
        if (level != null && level.intValue() == 1) return IssueType.EPIC;

        String name = ((String) it.get("name")).toUpperCase();
        if (name.contains("EPIC")) return IssueType.EPIC;
        if (name.contains("STORY") || name.contains("CÂU CHUYỆN")) return IssueType.STORY;
        if (name.contains("BUG") || name.contains("LỖI")) return IssueType.BUG;

        return IssueType.TASK;
    }

    @SuppressWarnings("unchecked")
    private String extractDescriptionFromJiraFields(Map<String, Object> fields) {
        Object rawDesc = fields.get("description");
        if (rawDesc == null) return "";

        if (rawDesc instanceof String) {
            return (String) rawDesc;
        }

        if (rawDesc instanceof Map) {
            return extractTextFromADF((Map<String, Object>) rawDesc);
        }

        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromADF(Map<String, Object> adfNode) {
        if (adfNode == null) return "";

        StringBuilder sb = new StringBuilder();

        if (adfNode.get("text") instanceof String text) {
            sb.append(text);
        }

        Object contentRaw = adfNode.get("content");
        if (contentRaw instanceof List) {
            List<Object> contentList = (List<Object>) contentRaw;
            for (Object child : contentList) {
                if (child instanceof Map) {
                    String childText = extractTextFromADF((Map<String, Object>) child);
                    if (!childText.isEmpty()) {
                        if (!sb.isEmpty() && !childText.startsWith("\n")) {
                            sb.append("\n");
                        }
                        sb.append(childText);
                    }
                }
            }
        }

        return sb.toString();
    }

    private IssueStatus mapJiraStatusToLocal(String jiraStatusName) {
        if (jiraStatusName == null) {
            return IssueStatus.TODO;
        }

        String status = jiraStatusName.toUpperCase();

        if (status.contains("DONE") || status.contains("CLOSED")) {
            return IssueStatus.DONE;
        }
        if (status.contains("IN PROGRESS") || status.contains("INPROGRESS")) {
            return IssueStatus.IN_PROGRESS;
        }
                if (status.contains("CANCELLED") || status.contains("CANCEL") || status.contains("REJECTED")) {
              return IssueStatus.DONE;
          }
        if (status.contains("TO DO") || status.contains("TODO") || status.contains("BACKLOG") || status.contains("OPEN")) {
            return IssueStatus.TODO;
        }

        return IssueStatus.TODO;
    }
}


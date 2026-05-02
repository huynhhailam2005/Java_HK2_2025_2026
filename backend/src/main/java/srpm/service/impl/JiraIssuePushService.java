package srpm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import srpm.model.Issue;
import srpm.model.IssueStatus;
import srpm.model.IssueType;
import srpm.model.SyncStatus;
import srpm.repository.IIssueRepository;
import srpm.service.IJiraIssuePushService;

import java.util.*;

@Service
@Transactional
public class JiraIssuePushService implements IJiraIssuePushService {

    private final IIssueRepository issueDao;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JiraIssuePushService.class);

    @Autowired
    public JiraIssuePushService(IIssueRepository issueDao, RestTemplate restTemplate) {
        this.issueDao = issueDao;
        this.restTemplate = restTemplate;
    }

    @Override
    public Issue createIssueOnJira(Issue issue, String jiraUrl, String projectKey, String adminEmail, String apiToken) {
        if (issue.getIssueType() == IssueType.SUB_TASK && issue.getParent() == null) {
            throw new RuntimeException("Lỗi: SubTask phải có Issue cha!");
        }

        if (issue.getParent() != null && issue.getParent().getIssueCode() == null) {
            logger.info("Issue cha chưa sync, tự động push cha: {} trước", issue.getParent().getTitle());
            try {
                createIssueOnJira(issue.getParent(), jiraUrl, projectKey, adminEmail, apiToken);
                issue.setParent(issueDao.findById(issue.getParent().getId()).orElse(issue.getParent()));
            } catch (Exception e) {
                logger.error("Lỗi khi push Issue cha: {}", e.getMessage());
                throw new RuntimeException("Không thể push Issue cha '" + issue.getParent().getTitle() + "': " + e.getMessage());
            }
        }

        try {
            String url = jiraUrl.replaceAll("/$", "") + "/rest/api/3/issue";
            Map<String, Object> requestBody = buildJiraRequest(issue, projectKey, true, jiraUrl, adminEmail, apiToken);

            HttpHeaders headers = createAuthHeaders(adminEmail, apiToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String jiraKey = (String) response.getBody().get("key");
                issue.setIssueCode(jiraKey);
                issue.setSyncStatus(SyncStatus.SYNCED);
                return issueDao.save(issue);
            }
            throw new RuntimeException("Jira trả về lỗi: " + response.getStatusCode());
        } catch (HttpClientErrorException e) {
            String msg = extractErrorMessage(e);
            issue.setSyncStatus(SyncStatus.ERROR);
            issueDao.save(issue);
            throw new RuntimeException("Lỗi Jira (Create): " + msg);
        }
    }

    @Override
    public Issue updateIssueOnJira(Issue issue, String jiraUrl, String adminEmail, String apiToken) {
        if (issue.getIssueCode() == null || issue.getIssueCode().isEmpty()) {
            throw new RuntimeException("Issue này chưa có Key Jira. Hãy dùng 'Push Create' trước!");
        }

        try {
            String url = jiraUrl.replaceAll("/$", "") + "/rest/api/3/issue/" + issue.getIssueCode();
            Map<String, Object> requestBody = buildJiraRequest(issue, null, false, jiraUrl, adminEmail, apiToken);

            HttpHeaders headers = createAuthHeaders(adminEmail, apiToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

            if (issue.getStatus() != null) {
                try {
                    String jiraStatusName = mapStatusToJiraName(issue.getStatus());
                    updateJiraStatus(
                            issue.getIssueCode(),
                            jiraStatusName,
                            jiraUrl,
                            adminEmail,
                            apiToken
                    );
                } catch (Exception e) {
                    logger.warn("Không thể đồng bộ trạng thái lên Jira: {}", e.getMessage());
                }
            }

            issue.setSyncStatus(SyncStatus.SYNCED);
            return issueDao.save(issue);
        } catch (HttpClientErrorException e) {
            String msg = extractErrorMessage(e);
            throw new RuntimeException("Lỗi Jira (Update): " + msg);
        }
    }

    private String getJiraIssueTypeName(Issue issue, String jiraUrl, String projectKey, String adminEmail, String apiToken) {
        if (issue.getIssueType() != IssueType.SUB_TASK) {
            String name = issue.getIssueType().name();
            return name.charAt(0) + name.substring(1).toLowerCase();
        }

        try {
            String url = jiraUrl.replaceAll("/$", "") + "/rest/api/3/issue/createmeta/" + projectKey + "/issuetypes";
            HttpHeaders headers = createAuthHeaders(adminEmail, apiToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> types = (List<Map<String, Object>>) response.getBody().get("values");
                if (types != null) {
                    for (Map<String, Object> type : types) {
                        if (Boolean.TRUE.equals(type.get("subtask"))) {
                            String name = (String) type.get("name");
                            logger.info("Tìm thấy Sub-task type name từ Jira: {}", name);
                            return name;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Không thể query issue types từ Jira, fallback về mặc định: {}", e.getMessage());
        }
        return "Subtask";
    }

    private Map<String, Object> buildJiraRequest(Issue issue, String projectKey, boolean isCreate,
                                                 String jiraUrl, String adminEmail, String apiToken) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("summary", issue.getTitle());
        fields.put("description", convertToADF(issue.getDescription()));

        if (isCreate) {
            fields.put("project", Map.of("key", projectKey));
            String typeName = getJiraIssueTypeName(issue, jiraUrl, projectKey, adminEmail, apiToken);
            fields.put("issuetype", Map.of("name", typeName));
        }

        if (issue.getParent() != null && issue.getParent().getIssueCode() != null) {
            fields.put("parent", Map.of("key", issue.getParent().getIssueCode()));

            if (issue.getIssueType() == IssueType.SUB_TASK) {
                logger.info("Gán parent (Sub-task) {} cho issue {}", issue.getParent().getIssueCode(), issue.getTitle());
            } else {
                logger.info("Gán Epic/Parent {} cho issue {}", issue.getParent().getIssueCode(), issue.getTitle());
            }
        }

        if (issue.getAssignedTo() != null && issue.getAssignedTo().getStudent() != null) {
            String jiraAccountId = issue.getAssignedTo().getStudent().getJiraAccountId();
            if (jiraAccountId != null && !jiraAccountId.isEmpty()) {
                fields.put("assignee", Map.of("accountId", jiraAccountId));
                logger.info("Gán assignee (accountId={}) cho issue {}", jiraAccountId, issue.getTitle());
            } else {
                logger.warn("Sinh viên {} chưa có Jira Account ID, bỏ qua assignee", issue.getAssignedTo().getStudent().getUsername());
            }
        } else if (!isCreate) {
            fields.put("assignee", null);
            logger.info("Clear assignee cho issue {} trên Jira", issue.getTitle());
        }

        if (issue.getDeadline() != null) {
            fields.put("duedate", issue.getDeadline().toLocalDate().toString());
        }
        return Map.of("fields", fields);
    }

    private Map<String, Object> convertToADF(String text) {
        String safeText = (text == null || text.isEmpty()) ? "No description" : text;
        return Map.of(
                "type", "doc", "version", 1,
                "content", List.of(Map.of(
                        "type", "paragraph",
                        "content", List.of(Map.of("type", "text", "text", safeText))
                ))
        );
    }

    private HttpHeaders createAuthHeaders(String email, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = Base64.getEncoder().encodeToString((email + ":" + token).getBytes());
        headers.set("Authorization", "Basic " + auth);
        return headers;
    }

    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            logger.error("Jira API error - Status: {}, Body: {}", e.getStatusCode(), body);

            if (body.contains("appropriate hierarchy") || body.contains("parent")) {
                return "Sai cấu trúc! Không thể gán issue này vào issue cha đã chọn trên Jira.";
            }

            Map map = objectMapper.readValue(body, Map.class);
            if (map.containsKey("errorMessages")) {
                Object msgs = map.get("errorMessages");
                if (msgs instanceof List && ((List<?>) msgs).isEmpty()) {
                    if (map.containsKey("errors")) return "Validation error: " + map.get("errors").toString();
                    return "Jira API error (" + e.getStatusCode() + "): Unknown field or invalid value";
                }
                return msgs.toString();
            }
            if (map.containsKey("errors")) return map.get("errors").toString();
        } catch (Exception ex) { return e.getStatusCode().toString(); }
        return e.getStatusText();
    }

    @Override public void deleteIssueOnJira(Issue issue, String url, String email, String token) {}

    @Override
    public void updateJiraStatus(String issueKey, String targetStatus, String jiraUrl, String adminEmail, String apiToken) {
        if (issueKey == null || targetStatus == null) return;

        try {
            String transitionsUrl = jiraUrl.replaceAll("/$", "") + "/rest/api/3/issue/" + issueKey + "/transitions";
            HttpHeaders headers = createAuthHeaders(adminEmail, apiToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(transitionsUrl, HttpMethod.GET, entity, Map.class);
            if (response.getBody() == null) return;

            List<Map<String, Object>> transitions = (List<Map<String, Object>>) response.getBody().get("transitions");
            if (transitions == null || transitions.isEmpty()) {
                logger.warn("Issue {} không có transitions nào khả dụng", issueKey);
                return;
            }

            for (Map<String, Object> transition : transitions) {
                Map<String, Object> to = (Map<String, Object>) transition.get("to");
                if (to != null) {
                    String toName = (String) to.get("name");
                    if (toName != null && toName.equalsIgnoreCase(targetStatus)) {
                        String transitionId = (String) transition.get("id");
                        if (transitionId == null) continue;

                        // Bước 3: Thực hiện transition
                        Map<String, Object> transitionBody = Map.of("transition", Map.of("id", transitionId));
                        HttpEntity<Map<String, Object>> transitionEntity = new HttpEntity<>(transitionBody, headers);

                        restTemplate.exchange(transitionsUrl, HttpMethod.POST, transitionEntity, Void.class);
                        logger.info("✓ Đã chuyển trạng thái {} -> {} trên Jira", issueKey, targetStatus);
                        return;
                    }
                }
            }

            logger.warn("Không tìm thấy transition '{}' cho issue {}", targetStatus, issueKey);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái trên Jira: {}", e.getMessage());
            throw new RuntimeException("Lỗi cập nhật trạng thái Jira: " + e.getMessage());
        }
    }

    private String mapStatusToJiraName(IssueStatus status) {
        switch (status) {
            case DONE:         return "Done";
            case IN_PROGRESS:  return "In Progress";
            case TODO:         return "To Do";
            default:           return "To Do";
        }
    }
}
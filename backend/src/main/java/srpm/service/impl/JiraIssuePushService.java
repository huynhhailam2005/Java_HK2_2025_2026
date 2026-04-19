package srpm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import srpm.model.Issue;
import srpm.model.IssueType;
import srpm.model.SyncStatus;
import srpm.repository.IssueRepository;
import srpm.service.IJiraIssuePushService;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý Push Issues từ SRPM lên Jira
 * Luồng thực tế: Tạo/Cập nhật/Xóa Issue trên SRPM -> Gọi API Jira để đồng bộ
 */
@Service
@Transactional
public class JiraIssuePushService implements IJiraIssuePushService {

    private final IssueRepository issueDao;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(JiraIssuePushService.class);

    @Autowired
    public JiraIssuePushService(
            IssueRepository issueDao,
            RestTemplate restTemplate
    ) {
        this.issueDao = issueDao;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // Tạo Issue mới trên Jira từ dữ liệu SRPM
    public Issue createIssueOnJira(
            Issue issue,
            String jiraUrl,
            String projectKey,
            String jiraAdminEmail,
            String jiraApiToken
    ) {
        try {
            // Đặt trạng thái PENDING trước khi push
            issue.setSyncStatus(SyncStatus.PENDING);
            issue = issueDao.save(issue);

            String urlBase = jiraUrl.replaceAll("/$", "") + "/rest/api/3/issue";

            // Xây dựng request body theo Jira Cloud API v3 (với ADF format)
            Map<String, Object> requestBody = buildCreateIssueRequest(issue, projectKey);

            HttpHeaders headers = createAuthHeaders(jiraAdminEmail, jiraApiToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            logger.info("Tạo Issue mới trên Jira: {} (loại: {})", issue.getTitle(), issue.getIssueType());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    urlBase,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String jiraKey = (String) response.getBody().get("key");
                issue.setIssueCode(jiraKey);
                issue.setSyncStatus(SyncStatus.SYNCED);
                issue = issueDao.save(issue);
                logger.info("✓ Tạo Issue thành công trên Jira với key: {}", jiraKey);
                return issue;
            } else {
                throw new RuntimeException("Jira trả về lỗi: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            // Xử lý lỗi HTTP từ Jira (4xx, 5xx)
            logger.error("Lỗi HTTP từ Jira API tạo Issue: {}", e.getStatusCode());
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString());
            issue.setSyncStatus(SyncStatus.ERROR);
            issueDao.save(issue);
            throw new RuntimeException("Lỗi tạo Issue trên Jira: " + errorMessage);
        } catch (RestClientException e) {
            logger.error("Lỗi khi gọi Jira API tạo Issue: {}", e.getMessage());
            issue.setSyncStatus(SyncStatus.ERROR);
            issueDao.save(issue);
            throw new RuntimeException("Lỗi tạo Issue trên Jira: " + e.getMessage());
        }
    }

    // Cập nhật Issue trên Jira
    public Issue updateIssueOnJira(
            Issue issue,
            String jiraUrl,
            String jiraAdminEmail,
            String jiraApiToken
    ) {
        if (issue.getIssueCode() == null || issue.getIssueCode().isEmpty()) {
            throw new RuntimeException("Issue không có issueCode, không thể cập nhật lên Jira");
        }

        try {
            String urlBase = jiraUrl.replaceAll("/$", "") + "/rest/api/3/issue/" + issue.getIssueCode();

            // Xây dựng request body với các thông tin cần cập nhật
            Map<String, Object> requestBody = buildUpdateIssueRequest(issue);

            HttpHeaders headers = createAuthHeaders(jiraAdminEmail, jiraApiToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            logger.info("Cập nhật Issue trên Jira: {} - {}", issue.getIssueCode(), issue.getTitle());
            ResponseEntity<Void> response = restTemplate.exchange(
                    urlBase,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                issue.setSyncStatus(SyncStatus.SYNCED);
                issue = issueDao.save(issue);
                logger.info("✓ Cập nhật Issue thành công trên Jira: {}", issue.getIssueCode());
                return issue;
            } else {
                throw new RuntimeException("Jira trả về lỗi: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Lỗi HTTP từ Jira API cập nhật Issue: {}", e.getStatusCode());
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString());
            issue.setSyncStatus(SyncStatus.ERROR);
            issueDao.save(issue);
            throw new RuntimeException("Lỗi cập nhật Issue trên Jira: " + errorMessage);
        } catch (RestClientException e) {
            logger.error("Lỗi khi gọi Jira API cập nhật Issue: {}", e.getMessage());
            issue.setSyncStatus(SyncStatus.ERROR);
            issueDao.save(issue);
            throw new RuntimeException("Lỗi cập nhật Issue trên Jira: " + e.getMessage());
        }
    }

    // Xóa Issue trên Jira (Soft-Delete trên SRPM)
    public void deleteIssueOnJira(
            Issue issue,
            String jiraUrl,
            String jiraAdminEmail,
            String jiraApiToken
    ) {
        if (issue.getIssueCode() == null || issue.getIssueCode().isEmpty()) {
            logger.warn("Issue không có issueCode, bỏ qua xóa trên Jira");
            issue.setIsDeleted(true);
            issueDao.save(issue);
            return;
        }

        try {
            String urlBase = jiraUrl.replaceAll("/$", "") + "/rest/api/3/issue/" + issue.getIssueCode();

            HttpHeaders headers = createAuthHeaders(jiraAdminEmail, jiraApiToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            logger.info("Xóa Issue trên Jira: {}", issue.getIssueCode());
            ResponseEntity<Void> response = restTemplate.exchange(
                    urlBase,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );

            // 204 No Content = Xóa thành công, 404 Not Found = Đã bị xóa trước đó
            if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                response.getStatusCode() == HttpStatus.NOT_FOUND) {
                issue.setIsDeleted(true);
                issue.setSyncStatus(SyncStatus.SYNCED);
                issueDao.save(issue);
                logger.info("✓ Xóa Issue thành công trên Jira: {}", issue.getIssueCode());
            } else {
                throw new RuntimeException("Jira trả về lỗi: " + response.getStatusCode().value());
            }
        } catch (HttpClientErrorException e) {
            // 404 = Đã bị xóa trước đó, coi như thành công
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                issue.setIsDeleted(true);
                issue.setSyncStatus(SyncStatus.SYNCED);
                issueDao.save(issue);
                logger.info("✓ Issue đã bị xóa trên Jira trước đó (404), cập nhật SRPM");
                return;
            }
            logger.error("Lỗi HTTP từ Jira API xóa Issue: {}", e.getStatusCode());
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString());
            issue.setSyncStatus(SyncStatus.ERROR);
            issueDao.save(issue);
            throw new RuntimeException("Lỗi xóa Issue trên Jira: " + errorMessage);
        } catch (RestClientException e) {
            logger.error("Lỗi khi gọi Jira API xóa Issue: {}", e.getMessage());
            issue.setSyncStatus(SyncStatus.ERROR);
            issueDao.save(issue);
            throw new RuntimeException("Lỗi xóa Issue trên Jira: " + e.getMessage());
        }
    }

    // ==================== Private Helper Methods ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildCreateIssueRequest(Issue issue, String projectKey) {
        Map<String, Object> fields = new HashMap<>();

        // ======== Project (bắt buộc) ========
        Map<String, Object> project = new HashMap<>();
        project.put("key", projectKey);
        fields.put("project", project);

        // ======== Summary / Title (bắt buộc) ========
        fields.put("summary", issue.getTitle());

        // ======== Description - Chuyển sang Atlassian Document Format (ADF) ========
        String description = issue.getDescription() != null ? issue.getDescription() : "";
        fields.put("description", convertToADF(description));

        // ======== Issue Type (bắt buộc) ========
        Map<String, Object> issueType = new HashMap<>();
        issueType.put("name", mapIssueTypeToJira(issue));
        fields.put("issuetype", issueType);

        // ======== Deadline (tuỳ chọn) ========
        if (issue.getDeadline() != null) {
            fields.put("duedate", issue.getDeadline().toLocalDate().toString());
        }

        // ======== Assignee - Dùng accountId (tuỳ chọn) ========
        if (issue.getAssignedTo() != null) {
            String jiraAccountId = issue.getAssignedTo().getStudent().getJiraAccountId();
            if (jiraAccountId != null && !jiraAccountId.isEmpty()) {
                Map<String, Object> assignee = new HashMap<>();
                assignee.put("accountId", jiraAccountId);
                fields.put("assignee", assignee);
            }
        }

        // ======== Parent Issue - Cho SUB_TASK ========
        if (issue.getIssueType() == IssueType.SUB_TASK && issue.getParent() != null) {
            String parentKey = issue.getParent().getIssueCode();
            if (parentKey != null && !parentKey.isEmpty()) {
                Map<String, Object> parent = new HashMap<>();
                parent.put("key", parentKey);
                fields.put("parent", parent);
            }
        }

        // ======== Xây dựng request object cuối cùng ========
        Map<String, Object> request = new HashMap<>();
        request.put("fields", fields);
        return request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildUpdateIssueRequest(Issue issue) {
        Map<String, Object> fields = new HashMap<>();

        // ======== Summary / Title ========
        fields.put("summary", issue.getTitle());

        // ======== Description - Chuyển sang Atlassian Document Format (ADF) ========
        String description = issue.getDescription() != null ? issue.getDescription() : "";
        fields.put("description", convertToADF(description));

        // ======== Deadline (tuỳ chọn) ========
        if (issue.getDeadline() != null) {
            fields.put("duedate", issue.getDeadline().toLocalDate().toString());
        }

        // ======== Assignee - Dùng accountId (tuỳ chọn) ========
        if (issue.getAssignedTo() != null) {
            String jiraAccountId = issue.getAssignedTo().getStudent().getJiraAccountId();
            if (jiraAccountId != null && !jiraAccountId.isEmpty()) {
                Map<String, Object> assignee = new HashMap<>();
                assignee.put("accountId", jiraAccountId);
                fields.put("assignee", assignee);
            }
        } else {
            // Nếu assignedTo là null, clear assignee trên Jira
            fields.put("assignee", null);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("fields", fields);
        return request;
    }

    private String mapIssueTypeToJira(Issue issue) {
        switch (issue.getIssueType()) {
            case EPIC:
                return "Epic";
            case STORY:
                return "Story";
            case BUG:
                return "Bug";
            case SUB_TASK:
                return "Sub-task";
            case TASK:
            default:
                return "Task";
        }
    }

    /**
     * Chuyển đổi plain text description sang Atlassian Document Format (ADF)
     *
     * ADF chuẩn cho mô tả:
     * {
     *   "type": "doc",
     *   "version": 1,
     *   "content": [
     *     {
     *       "type": "paragraph",
     *       "content": [
     *         {
     *           "type": "text",
     *           "text": "Nội dung mô tả"
     *         }
     *       ]
     *     }
     *   ]
     * }
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToADF(String plainText) {
        Map<String, Object> adf = new HashMap<>();
        adf.put("type", "doc");
        adf.put("version", 1);

        // Xây dựng content array
        List<Map<String, Object>> contentList = new ArrayList<>();

        // Tách text theo dòng (newline) để tạo từng paragraph
        String[] lines = plainText.isEmpty() ? new String[]{""} : plainText.split("\n");

        for (String line : lines) {
            Map<String, Object> paragraph = new HashMap<>();
            paragraph.put("type", "paragraph");

            // Content bên trong paragraph
            List<Map<String, Object>> textContent = new ArrayList<>();
            Map<String, Object> textObj = new HashMap<>();
            textObj.put("type", "text");
            textObj.put("text", line.isEmpty() ? " " : line); // Nếu dòng trống, thêm space
            textContent.add(textObj);

            paragraph.put("content", textContent);
            contentList.add(paragraph);
        }

        // Nếu không có nội dung, thêm paragraph trống
        if (contentList.isEmpty()) {
            Map<String, Object> emptyParagraph = new HashMap<>();
            emptyParagraph.put("type", "paragraph");
            List<Map<String, Object>> emptyContent = new ArrayList<>();
            Map<String, Object> emptyText = new HashMap<>();
            emptyText.put("type", "text");
            emptyText.put("text", "");
            emptyContent.add(emptyText);
            emptyParagraph.put("content", emptyContent);
            contentList.add(emptyParagraph);
        }

        adf.put("content", contentList);
        return adf;
    }

    /**
     * Trích xuất thông báo lỗi từ Jira API response
     * Jira API trả về error trong format: { "errorMessages": ["..."], "errors": {...} }
     */
    private String extractErrorMessage(String responseBody) {
        try {
            Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);

            // Kiểm tra errorMessages array
            if (errorResponse.containsKey("errorMessages")) {
                List<String> errorMessages = (List<String>) errorResponse.get("errorMessages");
                if (!errorMessages.isEmpty()) {
                    return errorMessages.get(0);
                }
            }

            // Kiểm tra errors object
            if (errorResponse.containsKey("errors")) {
                Map<String, Object> errors = (Map<String, Object>) errorResponse.get("errors");
                if (!errors.isEmpty()) {
                    return errors.values().iterator().next().toString();
                }
            }

            // Mặc định trả về toString
            return errorResponse.toString();
        } catch (Exception e) {
            return responseBody;
        }
    }

    /**
     * Cập nhật status của Issue trên Jira Cloud
     *
     * Jira Cloud không cho phép cập nhật trực tiếp field status.
     * Phải thực hiện qua 2 bước:
     * 1. Lấy Transition ID từ GET /rest/api/3/issue/{issueCode}/transitions
     * 2. Thực hiện transition: POST /rest/api/3/issue/{issueCode}/transitions
     */
    public void updateJiraStatus(
            String issueCode,
            String targetStatusName,
            String jiraUrl,
            String jiraAdminEmail,
            String jiraApiToken
    ) {
        try {
            String baseUrl = jiraUrl.replaceAll("/$", "");

            // ========== Step 1: Lấy danh sách transitions ==========
            String transitionsUrl = baseUrl + "/rest/api/3/issue/" + issueCode + "/transitions";
            HttpHeaders headers = createAuthHeaders(jiraAdminEmail, jiraApiToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            logger.info("Lấy transitions cho Issue: {}", issueCode);
            ResponseEntity<Map<String, Object>> transitionsResponse = restTemplate.exchange(
                    transitionsUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (!transitionsResponse.getStatusCode().is2xxSuccessful() || transitionsResponse.getBody() == null) {
                throw new RuntimeException("Lỗi lấy transitions từ Jira");
            }

            // ========== Step 2: Tìm Transition ID khớp với targetStatusName ==========
            Map<String, Object> transitionsData = transitionsResponse.getBody();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) transitionsData.get("transitions");

            String transitionId = null;
            if (transitions != null) {
                for (Map<String, Object> transition : transitions) {
                    Map<String, Object> toStatus = (Map<String, Object>) transition.get("to");
                    if (toStatus != null && targetStatusName.equalsIgnoreCase((String) toStatus.get("name"))) {
                        transitionId = (String) transition.get("id");
                        break;
                    }
                }
            }

            if (transitionId == null) {
                throw new RuntimeException("Không tìm thấy transition đến trạng thái: " + targetStatusName);
            }

            logger.info("✓ Tìm được transition ID: {} cho status: {}", transitionId, targetStatusName);

            // ========== Step 3: Thực hiện transition ==========
            String doTransitionUrl = baseUrl + "/rest/api/3/issue/" + issueCode + "/transitions";

            Map<String, Object> transitionPayload = new HashMap<>();
            Map<String, String> transitionData = new HashMap<>();
            transitionData.put("id", transitionId);
            transitionPayload.put("transition", transitionData);

            HttpEntity<Map<String, Object>> doTransitionEntity = new HttpEntity<>(transitionPayload, headers);

            logger.info("Thực hiện transition trên Jira: {} -> {}", issueCode, targetStatusName);
            ResponseEntity<Void> doTransitionResponse = restTemplate.exchange(
                    doTransitionUrl,
                    HttpMethod.POST,
                    doTransitionEntity,
                    Void.class
            );

            if (doTransitionResponse.getStatusCode().is2xxSuccessful()) {
                logger.info("✓ Cập nhật status thành công trên Jira: {} -> {}", issueCode, targetStatusName);
            } else {
                throw new RuntimeException("Jira trả về lỗi: " + doTransitionResponse.getStatusCode().value());
            }

        } catch (HttpClientErrorException e) {
            logger.error("Lỗi HTTP từ Jira khi cập nhật status: {}", e.getStatusCode());
            String errorMessage = extractErrorMessage(e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi cập nhật status trên Jira: " + errorMessage);
        } catch (RestClientException e) {
            logger.error("Lỗi khi gọi Jira API cập nhật status: {}", e.getMessage());
            throw new RuntimeException("Lỗi cập nhật status trên Jira: " + e.getMessage());
        }
    }

    private HttpHeaders createAuthHeaders(String adminEmail, String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = adminEmail + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }
}


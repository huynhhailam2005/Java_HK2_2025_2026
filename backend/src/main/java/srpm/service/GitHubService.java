package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import srpm.dto.response.GitHubMemberMappingDto;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.Student;
import srpm.repository.GroupRepository;
import srpm.repository.GroupMemberRepository;
import srpm.util.GitHubValidationUtil;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GitHubCollaboratorsCache collaboratorsCache;

    private static final String GITHUB_API_BASE = "https://api.github.com";

    /**
     * Lấy danh sách các thành viên nhóm và mapping với GitHub
     * @param groupId ID của nhóm
     * @return Danh sách thành viên với thông tin mapping GitHub
     */
    public List<GitHubMemberMappingDto> getGroupMemberMappings(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        // Lấy repository URL và token
        String repoUrl = group.getGithubRepoUrl();
        String accessToken = group.getGithubAccessToken();

        if (repoUrl == null || repoUrl.isEmpty()) {
            throw new RuntimeException("Nhóm chưa cấu hình GitHub repository URL");
        }

        // Extract owner/repo từ URL
        String[] repoParts = extractRepoOwnerAndName(repoUrl);
        String owner = repoParts[0];
        String repo = repoParts[1];

        // Lấy danh sách collaborators từ GitHub
        Map<String, String> githubCollaborators = fetchGitHubCollaborators(owner, repo, accessToken);

        // Lấy danh sách thành viên nhóm
        List<GroupMember> groupMembers = groupMemberRepository.findByGroup(groupId);

        // Map thành viên với GitHub info
        return groupMembers.stream()
                .map(member -> mapMemberToGitHub(member, group, githubCollaborators))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách collaborators từ GitHub
     * @param owner Owner của repository
     * @param repo Repository name
     * @param accessToken GitHub access token
     * @return Map of GitHub username -> permissions
     */
    private Map<String, String> fetchGitHubCollaborators(String owner, String repo, String accessToken) {
        String cacheKey = owner + "/" + repo;
        
        // Kiểm tra cache trước
        Map<String, String> cachedCollaborators = collaboratorsCache.get(cacheKey);
        if (cachedCollaborators != null) {
            logger.debug("Collaborators từ cache cho {}: {}", cacheKey, cachedCollaborators.keySet());
            return cachedCollaborators;
        }

        Map<String, String> collaborators = new HashMap<>();

        try {
            String url = String.format("%s/repos/%s/%s/collaborators", GITHUB_API_BASE, owner, repo);
            logger.info("Fetching collaborators từ: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            headers.set("User-Agent", "SRPM-App");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> body = response.getBody();
            logger.info("GitHub API response status: {}", response.getStatusCode());
            logger.info("Số lượng collaborators từ GitHub: {}", body != null ? body.size() : 0);

            if (body != null) {
                for (Map<String, Object> collaborator : body) {
                    Object loginObj = collaborator.get("login");
                    Object roleNameObj = collaborator.get("role_name");

                    if (loginObj != null && roleNameObj != null) {
                        String login = loginObj.toString();
                        String roleName = roleNameObj.toString();
                        String lowerLogin = login.toLowerCase();
                        collaborators.put(lowerLogin, roleName);
                        logger.debug("Collaborator: {} (role: {})", lowerLogin, roleName);
                    } else {
                        logger.warn("Collaborator bỏ qua vì thiếu login hoặc role_name: login={}, role_name={}",
                                loginObj, roleNameObj);
                    }
                }
            }

            logger.info("Danh sách collaborators cuối cùng: {}", collaborators.keySet());

            // Lưu vào cache
            collaboratorsCache.put(cacheKey, collaborators);
        } catch (RestClientException e) {
            logger.error("Lỗi khi lấy collaborators từ GitHub", e);
            throw new RuntimeException("Không thể lấy danh sách collaborators từ GitHub: " + e.getMessage());
        }

        return collaborators;
    }

     /**
      * Map thành viên nhóm với thông tin GitHub
      */
     private GitHubMemberMappingDto mapMemberToGitHub(GroupMember member, Group group,
                                                       Map<String, String> githubCollaborators) {
         Student student = member.getStudent();
         String githubUsername = student.getGithubUsername();

         // Trim whitespace from GitHub username
         if (githubUsername != null) {
             githubUsername = githubUsername.trim();
             student.setGithubUsername(githubUsername);
         }

         boolean isMapped = githubUsername != null && !githubUsername.isEmpty();
         boolean isCollaborator = isMapped && githubCollaborators.containsKey(githubUsername.toLowerCase());

         logger.debug("Checking member {} (GitHub: '{}'): isMapped={}, isCollaborator={}",
                 student.getStudentCode(),
                 githubUsername,
                 isMapped,
                 isCollaborator);

         if (isMapped) {
             logger.debug("  - Tìm kiếm '{}' trong collaborators: {}",
                     githubUsername.toLowerCase(),
                     githubCollaborators.keySet());
         }

         String mappingStatus = getMappingStatus(githubUsername, isCollaborator, githubCollaborators);

         return new GitHubMemberMappingDto(
                 member.getId(),
                 student.getId(),
                 student.getStudentCode(),
                 student.getUsername(),
                 githubUsername != null ? githubUsername : "",
                 group.getId(),
                 group.getGroupCode(),
                 group.getGroupName(),
                 group.getGithubRepoUrl(),
                 isMapped,
                 mappingStatus
         );
     }

    /**
     * Xác định trạng thái mapping
     */
    private String getMappingStatus(String githubUsername, boolean isCollaborator,
                                    Map<String, String> collaborators) {
        if (githubUsername == null || githubUsername.isEmpty()) {
            return "NOT_MAPPED";
        }

        if (!isCollaborator) {
            return "MAPPED_BUT_NOT_COLLABORATOR";
        }

        String roleName = collaborators.get(githubUsername.toLowerCase());

        // Map GitHub role names to permission levels
        if (roleName != null) {
            switch(roleName.toLowerCase()) {
                case "admin":
                    return "MAPPED_AS_ADMIN";
                case "maintain":
                    return "MAPPED_AS_MAINTAIN";
                case "write":
                    return "MAPPED_AS_WRITE";
                case "triage":
                    return "MAPPED_AS_TRIAGE";
                case "pull":
                    return "MAPPED_AS_READ";
                default:
                    return "MAPPED_AS_" + roleName.toUpperCase();
            }
        }

        return "MAPPED_BUT_NOT_COLLABORATOR";
    }

    /**
     * Lấy thông tin một thành viên và mapping GitHub
     */
    public GitHubMemberMappingDto getMemberMapping(Long groupId, Long memberId) {
        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        if (!member.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Thành viên không thuộc nhóm này");
        }

        String repoUrl = group.getGithubRepoUrl();
        String accessToken = group.getGithubAccessToken();

        if (repoUrl == null || repoUrl.isEmpty()) {
            throw new RuntimeException("Nhóm chưa cấu hình GitHub repository URL");
        }

        String[] repoParts = extractRepoOwnerAndName(repoUrl);
        Map<String, String> collaborators = fetchGitHubCollaborators(repoParts[0], repoParts[1], accessToken);

        return mapMemberToGitHub(member, group, collaborators);
    }

    /**
     * Thêm GitHub username cho thành viên nhóm
     */
    public GitHubMemberMappingDto addGitHubUsername(Long groupId, Long memberId, String githubUsername) {
        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        if (!member.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Thành viên không thuộc nhóm này");
        }

        // Validate GitHub username
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            throw new RuntimeException("GitHub username không được để trống");
        }

        if (!GitHubValidationUtil.isValidUsername(githubUsername)) {
            throw new RuntimeException("GitHub username không hợp lệ. " +
                    "Username chỉ được chứa ký tự alphanumeric và dashes, độ dài 1-39 ký tự");
        }

        Student student = member.getStudent();
        student.setGithubUsername(GitHubValidationUtil.normalizeUsername(githubUsername));

        // Map member với GitHub
        String repoUrl = group.getGithubRepoUrl();
        String accessToken = group.getGithubAccessToken();

        if (repoUrl != null && !repoUrl.isEmpty() && accessToken != null && !accessToken.isEmpty()) {
            String[] repoParts = extractRepoOwnerAndName(repoUrl);
            Map<String, String> collaborators = fetchGitHubCollaborators(repoParts[0], repoParts[1], accessToken);
            return mapMemberToGitHub(member, group, collaborators);
        }

        return mapMemberToGitHub(member, group, new HashMap<>());
    }

    /**
     * Xóa GitHub username của thành viên
     */
    public void removeGitHubUsername(Long groupId, Long memberId) {
        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        if (!member.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Thành viên không thuộc nhóm này");
        }

        Student student = member.getStudent();
        student.setGithubUsername(null);
    }

    /**
     * Lấy thống kê commits của một user trên repo
     * @param owner Owner của repository
     * @param repo Repository name
     * @param author GitHub username (author of commits)
     * @param accessToken GitHub access token
     * @return Stats về commits
     */
    public Map<String, Object> getCommitStats(String owner, String repo, String author, String accessToken) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Lấy danh sách commits của author
            String url = String.format("%s/repos/%s/%s/commits?author=%s&per_page=100",
                    GITHUB_API_BASE, owner, repo, author);
            logger.info("Fetching commits from: {}", url);

            HttpHeaders headers = new HttpHeaders();
            if (accessToken != null && !accessToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + accessToken);
            }
            headers.set("Accept", "application/vnd.github.v3+json");
            headers.set("User-Agent", "SRPM-App");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> commits = response.getBody();

            if (commits != null && !commits.isEmpty()) {
                stats.put("totalCommits", commits.size());
                stats.put("commits", commits);

                // Extract last commit info
                Map<String, Object> lastCommit = commits.get(0);
                stats.put("lastCommitDate", lastCommit.get("commit"));
                stats.put("lastCommitUrl", lastCommit.get("html_url"));

                logger.info("Found {} commits for author {} on {}/{}",
                        commits.size(), author, owner, repo);
            } else {
                stats.put("totalCommits", 0);
                stats.put("commits", new ArrayList<>());
                logger.info("No commits found for author {} on {}/{}", author, owner, repo);
            }

            stats.put("author", author);
            stats.put("repository", owner + "/" + repo);
            stats.put("statusCode", response.getStatusCode().toString());

        } catch (RestClientException e) {
            logger.error("Error fetching commits: {}", e.getMessage());
            stats.put("error", e.getMessage());
            stats.put("statusCode", "ERROR");
        }

        return stats;
    }

    /**
     * Lấy thống kê commits của tất cả thành viên trong nhóm
     * @param groupId ID của nhóm
     * @return Team commit summary
     */
    public Map<String, Object> getTeamCommitsSummary(Long groupId) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
            
            String repoUrl = group.getGithubRepoUrl();
            String accessToken = group.getGithubAccessToken();
            
            if (repoUrl == null || repoUrl.isEmpty() || accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Nhóm chưa cấu hình GitHub");
            }
            
            String[] repoParts = extractRepoOwnerAndName(repoUrl);
            String owner = repoParts[0];
            String repo = repoParts[1];
            
            // Lấy danh sách members
            List<GroupMember> members = groupMemberRepository.findByGroup(groupId);
            
            List<Map<String, Object>> memberStats = new ArrayList<>();
            int totalTeamCommits = 0;
            
            for (GroupMember member : members) {
                Student student = member.getStudent();
                String githubUsername = student.getGithubUsername();
                
                if (githubUsername == null || githubUsername.isEmpty()) {
                    continue;
                }
                
                try {
                    Map<String, Object> memberCommits = getCommitStats(owner, repo, githubUsername, accessToken);
                    
                    Integer commits = (Integer) memberCommits.getOrDefault("totalCommits", 0);
                    totalTeamCommits += commits;
                    
                    Map<String, Object> memberStat = new HashMap<>();
                    memberStat.put("memberId", member.getId());
                    memberStat.put("studentCode", student.getStudentCode());
                    memberStat.put("studentName", student.getUsername());
                    memberStat.put("githubUsername", githubUsername);
                    memberStat.put("totalCommits", commits);
                    memberStat.put("lastCommitUrl", memberCommits.get("lastCommitUrl"));
                    memberStat.put("lastCommitDate", memberCommits.get("lastCommitDate"));
                    memberStats.add(memberStat);
                    
                } catch (Exception e) {
                    logger.warn("Error getting commits for {}: {}", githubUsername, e.getMessage());
                }
            }
            
            // Sort by commits descending
            memberStats.sort((a, b) -> {
                Integer aCommits = (Integer) a.getOrDefault("totalCommits", 0);
                Integer bCommits = (Integer) b.getOrDefault("totalCommits", 0);
                return bCommits.compareTo(aCommits);
            });
            
            summary.put("groupId", groupId);
            summary.put("groupName", group.getGroupName());
            summary.put("groupCode", group.getGroupCode());
            summary.put("repository", owner + "/" + repo);
            summary.put("totalTeamCommits", totalTeamCommits);
            summary.put("totalMembers", members.size());
            summary.put("membersWithGitHub", memberStats.size());
            summary.put("memberStats", memberStats);
            summary.put("averageCommitsPerMember", memberStats.isEmpty() ? 0 : 
                    (double) totalTeamCommits / memberStats.size());
            
            logger.info("Team commits summary for group {}: {} total commits from {} members", 
                    groupId, totalTeamCommits, memberStats.size());
            
        } catch (Exception e) {
            logger.error("Error getting team commits summary: {}", e.getMessage());
            summary.put("error", e.getMessage());
        }
        
        return summary;
    }

    /**
     * Helper method: Extract owner và repo name từ GitHub URL
     * Hỗ trợ: https://github.com/owner/repo hoặc git@github.com:owner/repo.git
     */
    private String[] extractRepoOwnerAndName(String repoUrl) {
        try {
            return GitHubValidationUtil.extractOwnerAndRepo(repoUrl);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid GitHub repository URL format: " + repoUrl);
        }
    }


    /**
     * Debug: Lấy raw response từ GitHub API collaborators
     */
    public Map<String, Object> debugGetGitHubCollaborators(String owner, String repo, String accessToken) {
        Map<String, Object> debugInfo = new HashMap<>();

        try {
            String url = String.format("%s/repos/%s/%s/collaborators", GITHUB_API_BASE, owner, repo);
            debugInfo.put("url", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            headers.set("User-Agent", "SRPM-App");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            debugInfo.put("statusCode", response.getStatusCode().toString());
            debugInfo.put("body", response.getBody());
            debugInfo.put("headers", response.getHeaders());

            List<Map<String, Object>> body = response.getBody();
            if (body != null) {
                List<Map<String, String>> collaboratorsList = new ArrayList<>();
                for (Map<String, Object> collaborator : body) {
                    Map<String, String> collab = new HashMap<>();
                    collab.put("login", collaborator.getOrDefault("login", "").toString());
                    collab.put("permission", collaborator.getOrDefault("permission", "").toString());
                    collab.put("role_name", collaborator.getOrDefault("role_name", "").toString());
                    collaboratorsList.add(collab);
                }
                debugInfo.put("collaborators", collaboratorsList);
            }

            logger.info("DEBUG GitHub Collaborators: {}", debugInfo);
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            logger.error("DEBUG Error: {}", e.getMessage(), e);
        }

        return debugInfo;
    }
}


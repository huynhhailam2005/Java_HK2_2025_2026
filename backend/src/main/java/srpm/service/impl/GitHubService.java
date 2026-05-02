package srpm.service.impl;

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
import srpm.dto.GitHubMemberMappingDto;
import srpm.model.Group;
import srpm.model.GroupMember;
import srpm.model.Student;
import srpm.repository.IGroupMemberRepository;
import srpm.repository.IGroupRepository;
import srpm.service.IGitHubService;
import srpm.util.GitHubValidationUtil;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GitHubService implements IGitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    @Autowired
    private IGroupRepository groupDao;

    @Autowired
    private IGroupMemberRepository groupMemberDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GitHubCollaboratorsCacheService collaboratorsCache;

    private static final String GITHUB_API_BASE = "https://api.github.com";

    public List<GitHubMemberMappingDto> getGroupMemberMappings(Long groupId) {
        Group group = groupDao.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        String repoUrl = group.getGithubRepoUrl();
        String accessToken = group.getGithubAccessToken();

        if (repoUrl == null || repoUrl.isEmpty()) {
            throw new RuntimeException("Nhóm chưa cấu hình GitHub repository URL");
        }

        String[] repoParts = extractRepoOwnerAndName(repoUrl);
        String owner = repoParts[0];
        String repo = repoParts[1];

        Map<String, String> githubCollaborators = fetchGitHubCollaborators(owner, repo, accessToken);

        List<GroupMember> groupMembers = groupMemberDao.findByGroup(groupId);

        return groupMembers.stream()
                .map(member -> mapMemberToGitHub(member, group, githubCollaborators))
                .collect(Collectors.toList());
    }

    private Map<String, String> fetchGitHubCollaborators(String owner, String repo, String accessToken) {
        String cacheKey = owner + "/" + repo;

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

            collaboratorsCache.put(cacheKey, collaborators);
        } catch (RestClientException e) {
            logger.error("Lỗi khi lấy collaborators từ GitHub", e);
            throw new RuntimeException("Không thể lấy danh sách collaborators từ GitHub: " + e.getMessage());
        }

        return collaborators;
    }

    private GitHubMemberMappingDto mapMemberToGitHub(GroupMember member, Group group,
                                                       Map<String, String> githubCollaborators) {
        Student student = member.getStudent();
        String githubUsername = student.getGithubUsername();

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

    private String getMappingStatus(String githubUsername, boolean isCollaborator,
                                    Map<String, String> collaborators) {
        if (githubUsername == null || githubUsername.isEmpty()) {
            return "NOT_MAPPED";
        }

        if (!isCollaborator) {
            return "MAPPED_BUT_NOT_COLLABORATOR";
        }

        String roleName = collaborators.get(githubUsername.toLowerCase());

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

    public GitHubMemberMappingDto getMemberMapping(Long groupId, Long memberId) {
        GroupMember member = groupMemberDao.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Thành viên không tồn tại"));

        Group group = groupDao.findById(groupId)
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

    public Map<String, Object> getCommitStats(String owner, String repo, String author, String accessToken) {
        Map<String, Object> stats = new HashMap<>();

        try {
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

                Map<String, Object> lastCommit = commits.get(0);
                Map<String, Object> lastCommitInfo = (Map<String, Object>) lastCommit.get("commit");
                if (lastCommitInfo != null) {
                    Map<String, Object> lastAuthorInfo = (Map<String, Object>) lastCommitInfo.get("author");
                    if (lastAuthorInfo != null && lastAuthorInfo.get("date") != null) {
                        stats.put("lastCommitDate", lastAuthorInfo.get("date").toString());
                    }
                }
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

    public Map<String, Object> getTeamCommitsSummary(Long groupId) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Group group = groupDao.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
            
            String repoUrl = group.getGithubRepoUrl();
            String accessToken = group.getGithubAccessToken();
            
            if (repoUrl == null || repoUrl.isEmpty() || accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Nhóm chưa cấu hình GitHub");
            }
            
            String[] repoParts = extractRepoOwnerAndName(repoUrl);
            String owner = repoParts[0];
            String repo = repoParts[1];

            List<GroupMember> members = groupMemberDao.findByGroup(groupId);

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

    public Map<String, Object> getTeamCommitHistory(Long groupId, Integer days) {
        Map<String, Object> history = new HashMap<>();

        try {
            Group group = groupDao.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

            String repoUrl = group.getGithubRepoUrl();
            String accessToken = group.getGithubAccessToken();

            if (repoUrl == null || repoUrl.isEmpty() || accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Nhóm chưa cấu hình GitHub");
            }

            String[] repoParts = extractRepoOwnerAndName(repoUrl);
            String owner = repoParts[0];
            String repo = repoParts[1];

            if (days == null || days <= 0) {
                days = 30;
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -days);
            Date since = cal.getTime();

            List<GroupMember> members = groupMemberDao.findByGroup(groupId);

            Map<String, Map<String, Object>> dailyStats = new TreeMap<>(Collections.reverseOrder());
            Map<String, Integer> memberCommitCounts = new HashMap<>();
            int totalCommits = 0;

            for (GroupMember member : members) {
                Student student = member.getStudent();
                String githubUsername = student.getGithubUsername();

                if (githubUsername == null || githubUsername.isEmpty()) {
                    continue;
                }

                try {
                    List<Map<String, Object>> memberCommits = getCommitsInDateRange(owner, repo, githubUsername, accessToken, since);

                    for (Map<String, Object> commit : memberCommits) {
                        try {
                            Map<String, Object> commitInfo = (Map<String, Object>) commit.get("commit");
                            if (commitInfo != null) {
                                Map<String, Object> author = (Map<String, Object>) commitInfo.get("author");
                                if (author != null && author.get("date") != null) {
                                    String dateStr = author.get("date").toString();
                                    Date commitDate = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateStr);

                                    String dateKey = new java.text.SimpleDateFormat("yyyy-MM-dd").format(commitDate);

                                    dailyStats.computeIfAbsent(dateKey, k -> {
                                        Map<String, Object> dayStat = new HashMap<>();
                                        dayStat.put("date", dateKey);
                                        dayStat.put("totalCommits", 0);
                                        dayStat.put("commits", new ArrayList<Map<String, Object>>());
                                        dayStat.put("contributors", new HashMap<String, Integer>());
                                        return dayStat;
                                    });

                                    Map<String, Object> dayStat = dailyStats.get(dateKey);
                                    dayStat.put("totalCommits", (Integer) dayStat.get("totalCommits") + 1);

                                    List<Map<String, Object>> dayCommits = (List<Map<String, Object>>) dayStat.get("commits");
                                    Map<String, Object> commitSummary = new HashMap<>();
                                    commitSummary.put("sha", commit.get("sha"));
                                    Object rawMessage = commitInfo.get("message");
                                    commitSummary.put("message", rawMessage != null ? rawMessage.toString() : "");
                                    commitSummary.put("author", githubUsername);
                                    commitSummary.put("date", dateStr);
                                    commitSummary.put("url", commit.get("html_url"));
                                    dayCommits.add(commitSummary);

                                    Map<String, Integer> contributors = (Map<String, Integer>) dayStat.get("contributors");
                                    contributors.put(githubUsername, contributors.getOrDefault(githubUsername, 0) + 1);

                                    totalCommits++;
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing commit date: {}", e.getMessage());
                        }
                    }

                    memberCommitCounts.put(githubUsername, memberCommits.size());

                } catch (Exception e) {
                    logger.warn("Error getting commits for {}: {}", githubUsername, e.getMessage());
                }
            }

            List<Map<String, Object>> dailyStatsList = new ArrayList<>(dailyStats.values());
            dailyStatsList.sort((a, b) -> ((String) b.get("date")).compareTo((String) a.get("date")));

            history.put("groupId", groupId);
            history.put("groupName", group.getGroupName());
            history.put("repository", owner + "/" + repo);
            history.put("days", days);
            history.put("totalCommits", totalCommits);
            history.put("dailyStats", dailyStatsList);
            history.put("memberCommitCounts", memberCommitCounts);

            logger.info("Commit history for group {}: {} commits over {} days",
                    groupId, totalCommits, days);

        } catch (Exception e) {
            logger.error("Error getting team commit history: {}", e.getMessage());
            history.put("error", e.getMessage());
        }

        return history;
    }

    private List<Map<String, Object>> getCommitsInDateRange(String owner, String repo, String author,
                                                          String accessToken, Date since) {
        List<Map<String, Object>> allCommits = new ArrayList<>();

        try {
            String sinceStr = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(since);

            String url = String.format("%s/repos/%s/%s/commits?author=%s&since=%s&per_page=100",
                    GITHUB_API_BASE, owner, repo, author, sinceStr);

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
            if (commits != null) {
                allCommits.addAll(commits);
            }

        } catch (RestClientException e) {
            logger.error("Error fetching commits for {}: {}", author, e.getMessage());
        }

        return allCommits;
    }

    private String[] extractRepoOwnerAndName(String repoUrl) {
        try {
            return GitHubValidationUtil.extractOwnerAndRepo(repoUrl);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid GitHub repository URL format: " + repoUrl);
        }
    }

}


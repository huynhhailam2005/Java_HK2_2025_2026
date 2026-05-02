package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.dto.GitHubMemberMappingDto;
import srpm.model.Group;
import srpm.repository.IGroupRepository;
import srpm.service.impl.GitHubService;
import srpm.util.GitHubValidationUtil;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubMappingController {

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private IGroupRepository groupDao;

    @GetMapping("/groups/{groupId}/members")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> getGroupMemberMappings(@PathVariable Long groupId) {
        try {
            List<GitHubMemberMappingDto> mappings = gitHubService.getGroupMemberMappings(groupId);
            return ResponseEntity.ok(new ApiResponse(true, "Lấy danh sách mapping thành công", mappings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/groups/{groupId}/members/{memberId}/commits")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> getMemberCommitStats(@PathVariable Long groupId,
                                                             @PathVariable Long memberId) {
        try {
            GitHubMemberMappingDto memberMapping = gitHubService.getMemberMapping(groupId, memberId);

            if (memberMapping.getGithubUsername() == null || memberMapping.getGithubUsername().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Thành viên chưa cài đặt GitHub username", null));
            }

            if (!memberMapping.isMapped()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "GitHub username chưa được xác nhận hoặc không tồn tại", null));
            }

            Group group = groupDao.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

            String repoUrl = group.getGithubRepoUrl();
            String accessToken = group.getGithubAccessToken();

            if (repoUrl == null || repoUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Nhóm chưa cấu hình GitHub repository URL", null));
            }

            String[] repoParts = extractRepoOwnerAndName(repoUrl);
            String owner = repoParts[0];
            String repo = repoParts[1];

            Map<String, Object> stats = gitHubService.getCommitStats(owner, repo,
                    memberMapping.getGithubUsername(), accessToken);

            return ResponseEntity.ok(new ApiResponse(true, "Lấy thống kê commit thành công", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/groups/{groupId}/team-commits-summary")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> getTeamCommitsSummary(@PathVariable Long groupId) {
        try {
            Map<String, Object> summary = gitHubService.getTeamCommitsSummary(groupId);

            if (summary.containsKey("error")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, (String) summary.get("error"), null));
            }

            return ResponseEntity.ok(new ApiResponse(true, "Lấy thống kê team commits thành công", summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/groups/{groupId}/team-commit-history")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> getTeamCommitHistory(@PathVariable Long groupId,
                                                           @RequestParam(required = false, defaultValue = "30") Integer days) {
        try {
            Map<String, Object> history = gitHubService.getTeamCommitHistory(groupId, days);

            if (history.containsKey("error")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, (String) history.get("error"), null));
            }

            return ResponseEntity.ok(new ApiResponse(true, "Lấy lịch sử commit thành công", history));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    private String[] extractRepoOwnerAndName(String repoUrl) {
        try {
            return GitHubValidationUtil.extractOwnerAndRepo(repoUrl);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid GitHub repository URL format: " + repoUrl);
        }
    }
}

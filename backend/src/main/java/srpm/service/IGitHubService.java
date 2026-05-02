package srpm.service;

import srpm.dto.GitHubMemberMappingDto;

import java.util.List;
import java.util.Map;

public interface IGitHubService {

    List<GitHubMemberMappingDto> getGroupMemberMappings(Long groupId);

    GitHubMemberMappingDto getMemberMapping(Long groupId, Long memberId);

    Map<String, Object> getCommitStats(String owner, String repo, String author, String accessToken);

    Map<String, Object> getTeamCommitsSummary(Long groupId);

    Map<String, Object> getTeamCommitHistory(Long groupId, Integer days);
}

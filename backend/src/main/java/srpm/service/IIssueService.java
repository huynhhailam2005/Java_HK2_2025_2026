package srpm.service;

import srpm.dto.request.CreateIssueRequest;
import srpm.dto.request.UpdateIssueRequest;
import srpm.dto.IssueDetailDto;
import srpm.model.Issue;
import srpm.model.User;

import java.util.List;
import java.util.Map;

public interface IIssueService {

    Issue createIssue(CreateIssueRequest request);

    Issue updateIssue(Long issueId, UpdateIssueRequest request);

    Map<String, Object> updateIssueStatus(Long issueId, String newStatusStr, User currentUser,
                                           boolean isTeamLeader, boolean isAssignee);

    List<IssueDetailDto> getIssuesByGroup(Long groupId);

    List<IssueDetailDto> getMyAssignedIssues(Long studentId);
}


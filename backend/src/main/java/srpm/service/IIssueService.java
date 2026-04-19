package srpm.service;

import srpm.dto.request.CreateIssueRequest;
import srpm.model.Issue;

public interface IIssueService {

    Issue createIssue(CreateIssueRequest request);
}


package srpm.service;

import srpm.model.Submission;

public interface ISubmissionService {

    Submission submitForIssue(
            Long issueId,
            Long groupMemberId,
            String content
    );
}


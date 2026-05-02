package srpm.service;

import srpm.model.Student;
import srpm.model.Submission;

import java.util.List;
import java.util.Map;

public interface ISubmissionService {

    Submission submitForIssue(
            Long issueId,
            Long groupMemberId,
            String content
    );

    Submission submitIssue(Long issueId, String content, Student student);

    boolean isIssueSubmitted(Long issueId);

    List<Submission> getSubmissionsByGroup(Long groupId);
}


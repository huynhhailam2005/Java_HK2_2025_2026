package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import srpm.dao.ISubmissionDAO;
import srpm.model.Submission;

@Service
public class SubmissionService {

    @Autowired
    private ISubmissionDAO submissionDAO;

    public void submit(Submission submission) {
        submissionDAO.createSubmission(submission);
    }
}
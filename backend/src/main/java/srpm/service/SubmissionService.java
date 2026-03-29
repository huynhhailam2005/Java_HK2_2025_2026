package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.model.Submission;
import srpm.repository.SubmissionRepository;

@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    @Autowired
    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public void submit(Submission submission) {
        submissionRepository.save(submission);
    }
}
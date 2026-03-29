package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}


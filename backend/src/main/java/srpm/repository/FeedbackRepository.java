package srpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import srpm.model.Feedback;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {

    List<Feedback> findByGroupIdOrderByCreatedAtDesc(String groupId);
}


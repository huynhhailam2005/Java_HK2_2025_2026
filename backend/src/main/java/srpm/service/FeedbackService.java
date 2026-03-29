package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpm.model.Feedback;
import srpm.repository.FeedbackRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }


    @Transactional
    public String createFeedback(Feedback feedback) {

        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Số sao đánh giá phải nằm trong khoảng từ 1 đến 5!");
        }

        feedback.setId(UUID.randomUUID().toString());

        feedback.setCreatedAt(LocalDateTime.now());


        feedbackRepository.save(feedback);

        return "Tạo đánh giá thành công!";
    }


    @Transactional(readOnly = true)
    public List<Feedback> getFeedbacksByGroup(String groupId) {
        return feedbackRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }
}
package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import srpm.dao.IFeedbackDAO;
import srpm.model.Feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service 
public class FeedbackService {

    @Autowired
    private IFeedbackDAO feedbackDAO; 


    public String createFeedback(Feedback feedback) {

        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Số sao đánh giá phải nằm trong khoảng từ 1 đến 5!");
        }
     
        feedback.setId(UUID.randomUUID().toString());
        
        feedback.setCreatedAt(LocalDateTime.now());
        
  
        feedbackDAO.insertFeedback(feedback);
        
        return "Tạo đánh giá thành công!";
    }

   
    public List<Feedback> getFeedbacksByGroup(String groupId) {
        return feedbackDAO.getFeedbacksByGroupId(groupId);
    }
}
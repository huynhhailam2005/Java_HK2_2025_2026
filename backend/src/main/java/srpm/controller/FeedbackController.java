package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.model.Feedback;
import srpm.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks") 
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

  
    @PostMapping
    public ResponseEntity<String> submitFeedback(@RequestBody Feedback feedback) {
        try {
            String message = feedbackService.createFeedback(feedback);
            return ResponseEntity.ok(message); 
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }

 
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Feedback>> getGroupFeedbacks(@PathVariable String groupId) {
        List<Feedback> feedbacks = feedbackService.getFeedbacksByGroup(groupId);
        return ResponseEntity.ok(feedbacks);
    }
}
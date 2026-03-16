package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.request.TopicRequest;
import srpm.dto.response.TopicResponse;
import srpm.model.Topic;
import srpm.service.TopicService;

import java.util.Optional;

@RestController
@RequestMapping("/api/topics")
@CrossOrigin(origins = "http://localhost:5173")
public class TopicRestController {

    private final TopicService topicService;

    @Autowired
    public TopicRestController(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * Đăng ký đề tài mới
     * POST /api/topics
     */
    @PostMapping
    public ResponseEntity<TopicResponse> createTopic(@RequestBody TopicRequest request) {
        try {
            Topic created = topicService.createTopic(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new TopicResponse(true, "Đăng ký đề tài thành công", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new TopicResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TopicResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

    /**
     * Xem chi tiết đề tài
     * GET /api/topics/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TopicResponse> getTopicById(@PathVariable String id) {
        Optional<Topic> topic = topicService.getTopicById(id);
        if (topic.isPresent()) {
            return ResponseEntity.ok(new TopicResponse(true, "Thành công", topic.get()));
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new TopicResponse(false, "Không tìm thấy đề tài với ID: " + id, null));
    }

    /**
     * Chỉnh sửa thông tin đề tài
     * PUT /api/topics/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable String id,
                                                     @RequestBody TopicRequest request) {
        try {
            Topic updated = topicService.updateTopic(id, request);
            return ResponseEntity.ok(new TopicResponse(true, "Cập nhật đề tài thành công", updated));
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().startsWith("Không tìm thấy")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity
                    .status(status)
                    .body(new TopicResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TopicResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }
}

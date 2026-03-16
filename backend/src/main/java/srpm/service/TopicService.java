package srpm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import srpm.dao.ITopicDAO;
import srpm.dto.request.TopicRequest;
import srpm.model.Topic;
import srpm.model.TopicStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TopicService {

    private final ITopicDAO topicDAO;

    @Autowired
    public TopicService(ITopicDAO topicDAO) {
        this.topicDAO = topicDAO;
    }

    /**
     * Đăng ký đề tài mới.
     * Status mặc định là PENDING (chờ duyệt).
     */
    public Topic createTopic(TopicRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đề tài không được để trống");
        }
        if (request.getLecturerId() == null || request.getLecturerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Lecturer ID không được để trống");
        }

        try {
            Topic topic = new Topic();
            topic.setId(UUID.randomUUID().toString());
            topic.setTitle(request.getTitle().trim());
            topic.setDescription(request.getDescription());
            topic.setStatus(TopicStatus.PENDING);
            topic.setLecturerId(request.getLecturerId().trim());
            topic.setStudentId(request.getStudentId());
            topic.setCreatedAt(LocalDateTime.now());
            topic.setUpdatedAt(LocalDateTime.now());

            topicDAO.save(topic);
            return topic;
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi đăng ký đề tài", ex);
        }
    }

    /**
     * Chỉnh sửa thông tin đề tài.
     * Chỉ cập nhật title và description (status do admin duyệt riêng).
     */
    public Topic updateTopic(String id, TopicRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đề tài không được để trống");
        }

        try {
            Topic existing = topicDAO.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy đề tài với ID: " + id);
            }

            existing.setTitle(request.getTitle().trim());
            existing.setDescription(request.getDescription());
            if (request.getLecturerId() != null && !request.getLecturerId().trim().isEmpty()) {
                existing.setLecturerId(request.getLecturerId().trim());
            }
            if (request.getStudentId() != null) {
                existing.setStudentId(request.getStudentId());
            }
            existing.setUpdatedAt(LocalDateTime.now());

            topicDAO.update(existing);
            return existing;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi cập nhật đề tài", ex);
        }
    }

    /**
     * Xem chi tiết đề tài theo ID.
     */
    public Optional<Topic> getTopicById(String id) {
        try {
            return Optional.ofNullable(topicDAO.findById(id));
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Lỗi DB khi tìm đề tài", ex);
        }
    }
}

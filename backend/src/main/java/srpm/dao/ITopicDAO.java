package srpm.dao;

import srpm.model.Topic;
import java.util.List;

public interface ITopicDAO {
    Topic findById(String id);
    List<Topic> findAll();
    List<Topic> findByLecturerId(String lecturerId);
    void save(Topic topic);
    void update(Topic topic);
    void deleteById(String id);
}

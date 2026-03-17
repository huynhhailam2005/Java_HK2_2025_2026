package srpm.dao;

import srpm.model.Feedback;
import java.util.List;

public interface IFeedbackDAO {
    void insertFeedback(Feedback feedback);
    List<Feedback> getFeedbacksByGroupId(String groupId);
}
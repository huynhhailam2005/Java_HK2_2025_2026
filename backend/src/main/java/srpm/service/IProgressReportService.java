package srpm.service;

import java.util.Map;

public interface IProgressReportService {

    Map<String, Object> generateProgressReport(Long groupId);
}


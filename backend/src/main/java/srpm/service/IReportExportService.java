package srpm.service;

public interface IReportExportService {
    byte[] exportGroupProgressPdf(Long groupId);
}

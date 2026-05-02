package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.exception.ForbiddenException;
import srpm.model.Group;
import srpm.repository.IGroupRepository;
import srpm.service.IAuthorizationService;
import srpm.service.IReportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportExportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportExportController.class);

    private final IReportExportService reportExportService;
    private final IAuthorizationService authorizationService;
    private final IGroupRepository IGroupRepository;

    @Autowired
    public ReportExportController(IReportExportService reportExportService,
                                  IAuthorizationService authorizationService,
                                  IGroupRepository IGroupRepository) {
        this.reportExportService = reportExportService;
        this.authorizationService = authorizationService;
        this.IGroupRepository = IGroupRepository;
    }

    @GetMapping("/group/{groupId}/export")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<byte[]> exportGroupReport(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "pdf") String format) {

        logger.info("Export request for group {} in format: {}", groupId, format);

        if (!authorizationService.canAccessGroup(groupId)) {
            logger.warn("Access denied for group: {}", groupId);
            throw new ForbiddenException("Bạn không có quyền xem báo cáo nhóm này");
        }

        if (!"pdf".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("Định dạng không được hỗ trợ: " + format);
        }

        Group group = IGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại: " + groupId));
        String groupCode = group.getGroupCode() != null ? group.getGroupCode() : "Nhom" + groupId;
        String fileName = String.format("BaoCao_TienDo_%s.pdf", groupCode);

        byte[] pdfBytes = reportExportService.exportGroupProgressPdf(groupId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName)
                .build());
        headers.setContentLength(pdfBytes.length);

        logger.info("PDF exported successfully for group {}: {} bytes", groupId, pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}

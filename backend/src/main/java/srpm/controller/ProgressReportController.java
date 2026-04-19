package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.exception.ForbiddenException;
import srpm.service.IProgressReportService;
import srpm.service.IAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "http://localhost:5173")
public class ProgressReportController {

    private static final Logger logger = LoggerFactory.getLogger(ProgressReportController.class);
    private final IProgressReportService progressReportService;
    private final IAuthorizationService authorizationService;

    @Autowired
    public ProgressReportController(IProgressReportService progressReportService,
                                   IAuthorizationService authorizationService) {
        this.progressReportService = progressReportService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<ApiResponse> getGroupProgressReport(@PathVariable Long groupId) {
        logger.debug("Fetching progress report for group: {}", groupId);

        if (!authorizationService.canAccessGroup(groupId)) {
            logger.warn("Access denied for group: {}", groupId);
            throw new ForbiddenException("Bạn không có quyền xem báo cáo nhóm này");
        }

        Map<String, Object> report = progressReportService.generateProgressReport(groupId);
        logger.info("Progress report generated for group: {}", groupId);
        return ResponseEntity.ok(new ApiResponse(true, "Lấy báo cáo tiến độ thành công", report));
    }

}


package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.dto.IssueDetailDto;
import srpm.model.Student;
import srpm.model.User;
import srpm.service.IAuthorizationService;
import srpm.service.IIssueService;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    private final IIssueService issueService;
    private final IAuthorizationService authorizationService;

    @Autowired
    public IssueController(IIssueService issueService, IAuthorizationService authorizationService) {
        this.issueService = issueService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse> getIssuesByGroup(@PathVariable Long groupId) {
        try {
            System.out.println("Đang lấy task cho nhóm ID: " + groupId);
            List<IssueDetailDto> issueDtos = issueService.getIssuesByGroup(groupId);
            return ResponseEntity.ok(new ApiResponse(true, "Thành công", issueDtos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(false, "Lỗi Server: " + e.getMessage(), null));
        }
    }

    @GetMapping("/my-assigned")
    public ResponseEntity<ApiResponse> getMyAssignedIssues() {
        try {
            User user = authorizationService.getCurrentUser().orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(new ApiResponse(false, "Chưa đăng nhập", null));
            }

            if (!(user instanceof Student student)) {
                return ResponseEntity.ok(new ApiResponse(true, "OK", List.of()));
            }

            List<IssueDetailDto> issueDtos = issueService.getMyAssignedIssues(student.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Thành công", issueDtos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ApiResponse(false, "Lỗi Server: " + e.getMessage(), null));
        }
    }
}
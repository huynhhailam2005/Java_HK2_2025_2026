package srpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import srpm.dto.response.ApiResponse;
import srpm.service.impl.GitHubCollaboratorsCache;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin controller để quản lý GitHub cache
 */
@RestController
@RequestMapping("/api/admin/github-cache")
@PreAuthorize("hasRole('ADMIN')")
public class GitHubCacheController {

    @Autowired
    private GitHubCollaboratorsCache collaboratorsCache;

    // Lấy thống kê cache
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", collaboratorsCache.size());
        stats.put("message", "Cache hiện có " + collaboratorsCache.size() + " entries");

        return ResponseEntity.ok(new ApiResponse(true, "Lấy thống kê cache thành công", stats));
    }

    // Clear cache cho một repository
    @DeleteMapping("/{owner}/{repo}")
    public ResponseEntity<ApiResponse> invalidateCache(@PathVariable String owner,
                                                        @PathVariable String repo) {
        String cacheKey = owner + "/" + repo;
        collaboratorsCache.invalidate(cacheKey);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Cache đã được xóa cho " + cacheKey);

        return ResponseEntity.ok(new ApiResponse(true, "Xóa cache thành công", result));
    }

    // Clear toàn bộ cache
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearAllCache() {
        collaboratorsCache.clear();

        Map<String, String> result = new HashMap<>();
        result.put("message", "Toàn bộ cache đã được xóa");

        return ResponseEntity.ok(new ApiResponse(true, "Clear cache thành công", result));
    }
}


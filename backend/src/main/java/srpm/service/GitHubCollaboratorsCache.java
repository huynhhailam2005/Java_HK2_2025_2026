package srpm.service;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache cho GitHub collaborators data
 * TTL: 1 giờ
 */
@Component
public class GitHubCollaboratorsCache {

    private static class CacheEntry {
        Map<String, String> collaborators;
        long timestamp;

        CacheEntry(Map<String, String> collaborators) {
            this.collaborators = collaborators;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            // TTL: 1 giờ (3600000 ms)
            return System.currentTimeMillis() - timestamp > 3600000;
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Lấy collaborators từ cache
     * @param cacheKey Format: "{owner}/{repo}"
     * @return Collaborators map hoặc null nếu không có trong cache hoặc hết hạn
     */
    public Map<String, String> get(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return new HashMap<>(entry.collaborators);
        }
        // Xóa entry hết hạn
        cache.remove(cacheKey);
        return null;
    }

    /**
     * Lưu collaborators vào cache
     */
    public void put(String cacheKey, Map<String, String> collaborators) {
        cache.put(cacheKey, new CacheEntry(collaborators));
    }

    /**
     * Clear cache cho một repository
     */
    public void invalidate(String cacheKey) {
        cache.remove(cacheKey);
    }

    /**
     * Clear toàn bộ cache
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Lấy số lượng entry trong cache
     */
    public int size() {
        return cache.size();
    }
}


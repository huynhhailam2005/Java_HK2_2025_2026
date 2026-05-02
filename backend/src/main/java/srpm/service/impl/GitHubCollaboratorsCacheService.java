package srpm.service.impl;

import org.springframework.stereotype.Component;
import srpm.service.IGitHubCollaboratorsCacheService; // Import interface

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GitHubCollaboratorsCacheService implements IGitHubCollaboratorsCacheService {

    private static class CacheEntry {
        Map<String, String> collaborators;
        long timestamp;

        CacheEntry(Map<String, String> collaborators) {
            this.collaborators = collaborators;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            // Hết hạn sau 1 giờ (3600000 ms)
            return System.currentTimeMillis() - timestamp > 3600000;
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> get(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return new HashMap<>(entry.collaborators);
        }
        cache.remove(cacheKey);
        return null;
    }

    @Override
    public void put(String cacheKey, Map<String, String> collaborators) {
        cache.put(cacheKey, new CacheEntry(collaborators));
    }

    @Override
    public int size() {
        return cache.size();
    }
}
package srpm.service;

import java.util.Map;

public interface IGitHubCollaboratorsCacheService {
    Map<String, String> get(String cacheKey);

    void put(String cacheKey, Map<String, String> collaborators);

    int size();
}
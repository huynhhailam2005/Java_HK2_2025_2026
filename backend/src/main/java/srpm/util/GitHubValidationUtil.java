package srpm.util;

import java.util.regex.Pattern;

public class GitHubValidationUtil {

    private static final Pattern GITHUB_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$");
    private static final Pattern HTTPS_REPO_URL_PATTERN = Pattern.compile("^https://github\\.com/[a-zA-Z0-9_-]+/[a-zA-Z0-9_.-]+(\\.git)?$");
    private static final Pattern SSH_REPO_URL_PATTERN = Pattern.compile("^git@github\\.com:[a-zA-Z0-9_-]+/[a-zA-Z0-9_.-]+(\\.git)?$");

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty() || username.length() > 39) {
            return false;
        }
        return GITHUB_USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidRepositoryUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return HTTPS_REPO_URL_PATTERN.matcher(url).matches() ||
               SSH_REPO_URL_PATTERN.matcher(url).matches();
    }

    public static boolean isValidAccessToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return (token.startsWith("ghp_") || token.startsWith("ghu_")) && token.length() > 10;
    }

    public static String extractOwner(String repoUrl) throws IllegalArgumentException {
        String[] parts = extractOwnerAndRepo(repoUrl);
        return parts[0];
    }

    public static String extractRepoName(String repoUrl) throws IllegalArgumentException {
        String[] parts = extractOwnerAndRepo(repoUrl);
        return parts[1];
    }

    public static String[] extractOwnerAndRepo(String repoUrl) throws IllegalArgumentException {
        if (!isValidRepositoryUrl(repoUrl)) {
            throw new IllegalArgumentException("Invalid GitHub repository URL: " + repoUrl);
        }

        String url = repoUrl.trim();
        String[] parts = null;

        if (url.contains("https://github.com/")) {
            url = url.replace("https://github.com/", "").replace(".git", "");
            parts = url.split("/");
        } else if (url.contains("git@github.com:")) {
            url = url.replace("git@github.com:", "").replace(".git", "");
            parts = url.split("/");
        }

        if (parts != null && parts.length >= 2) {
            return new String[]{parts[parts.length - 2], parts[parts.length - 1]};
        }

        throw new IllegalArgumentException("Invalid GitHub repository URL format: " + repoUrl);
    }

    public static String sshToHttpsUrl(String sshUrl) throws IllegalArgumentException {
        String[] parts = extractOwnerAndRepo(sshUrl);
        return String.format("https://github.com/%s/%s", parts[0], parts[1]);
    }

    public static String normalizeUsername(String username) {
        return username != null ? username.toLowerCase().trim() : null;
    }
}


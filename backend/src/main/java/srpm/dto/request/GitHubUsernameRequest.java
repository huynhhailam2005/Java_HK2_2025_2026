package srpm.dto.request;

public class GitHubUsernameRequest {
    private String githubUsername;

    public GitHubUsernameRequest() {}

    public GitHubUsernameRequest(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }
}


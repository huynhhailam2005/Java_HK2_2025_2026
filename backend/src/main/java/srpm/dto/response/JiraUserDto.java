package srpm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JiraUserDto {

    @JsonProperty("accountId")
    private String accountId;

    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("active")
    private boolean active;

    public JiraUserDto() {}

    public JiraUserDto(String accountId, String emailAddress, String displayName, boolean active) {
        this.accountId = accountId;
        this.emailAddress = emailAddress;
        this.displayName = displayName;
        this.active = active;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}


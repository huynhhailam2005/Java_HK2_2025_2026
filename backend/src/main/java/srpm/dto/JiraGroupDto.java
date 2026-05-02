package srpm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JiraGroupDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("members")
    private List<JiraUserDto> members;

    public JiraGroupDto() {}

    public JiraGroupDto(String name, List<JiraUserDto> members) {
        this.name = name;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JiraUserDto> getMembers() {
        return members;
    }

    public void setMembers(List<JiraUserDto> members) {
        this.members = members;
    }
}


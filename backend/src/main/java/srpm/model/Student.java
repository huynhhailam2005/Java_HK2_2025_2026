package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("STUDENT")
public class Student extends User {

    @Column(name = "student_code", length = 20, nullable = false, unique = true)
    private String studentCode;

    @Column(name = "jira_account_id", unique = true, length = 255)
    private String jiraAccountId;

    @Column(name = "github_username", unique = true, length = 255)
    private String githubUsername;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private Set<GroupMember> groupMembers = new HashSet<>();

    public Student() {
        super();
    }

    public Student(String username, String password, String email, UserRole userRole, String studentCode) {
        super(username, password, email, userRole);
        this.studentCode = studentCode;
    }

    public Long getId() {
        return this.getID();
    }

    public String getStudentCode() { return this.studentCode; }
    public String getJiraAccountId() { return this.jiraAccountId; }
    public String getGithubUsername() { return this.githubUsername; }
    public Set<GroupMember> getGroupMembers() { return this.groupMembers; }

    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public void setJiraAccountId(String jiraAccountId) { this.jiraAccountId = jiraAccountId; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }
    public void setGroupMembers(Set<GroupMember> groupMembers) { this.groupMembers = groupMembers; }
}


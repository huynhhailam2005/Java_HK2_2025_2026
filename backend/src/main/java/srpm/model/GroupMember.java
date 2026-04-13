package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "group_member_role", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private GroupMemberRole groupMemberRole = GroupMemberRole.TEAM_MEMBER;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
    }

    public GroupMember() {}

    public GroupMember(Group group, Student student, GroupMemberRole groupMemberRole) {
        this.group = group;
        this.student = student;
        this.groupMemberRole = groupMemberRole;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Group getGroup() { return group; }
    public Student getStudent() { return student; }
    public GroupMemberRole getGroupMemberRole() { return groupMemberRole; }
    public LocalDateTime getJoinedAt() { return joinedAt; }

    public void setId(Long id) { this.id = id; }
    public void setGroup(Group group) { this.group = group; }
    public void setStudent(Student student) { this.student = student; }
    public void setGroupMemberRole(GroupMemberRole memberRole) { this.groupMemberRole = memberRole; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}




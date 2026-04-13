package srpm.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    @Column(name = "admin_code", length = 20, nullable = false, unique = true)
    private String adminCode;

    public Admin() {
        super();
    }

    public Admin(String username, String password, String email, UserRole userRole, String adminCode) {
        super(username, password, email, userRole);
        this.adminCode = adminCode;
    }

    public Long getId() {
        return this.getID();
    }

    public String getAdminCode() { return this.adminCode; }

    public void setAdminCode(String adminCode) { this.adminCode = adminCode; }
}


package admin;
import java.sql.Timestamp;
public class UserDTO {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String roleName; // STUDENT, MODERATOR, EXAM_AUTHOR, TEACHER, ADMIN
    private String status;   // ACTIVE, INACTIVE, DELETED
    private Timestamp lastLogin;
    public UserDTO() {
    }
    public UserDTO(String username, String email, String passwordHash, String roleName) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleName = roleName;
        this.status = "ACTIVE";
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Timestamp getLastLogin() {
        return lastLogin;
    }
    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roleName='" + roleName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

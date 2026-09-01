package dao;

import java.sql.Timestamp;
import java.util.List;

public interface AuditLogDAO {
    void logAction(int userId, String action);
    List<AuditEntry> getAllLogs();
    List<AuditEntry> getLogsByUser(int userId);

    class AuditEntry {
        private int logId;
        private Integer userId;
        private String username;
        private String email;
        private String action;
        private Timestamp timestamp;

        public int getLogId() { return logId; }
        public void setLogId(int logId) { this.logId = logId; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Timestamp getTimestamp() { return timestamp; }
        public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    }
}

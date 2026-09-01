package dao;

import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {

    @Override
    public void logAction(int userId, String action) {
        String sql = "INSERT INTO audit_log (user_id, action, timestamp) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            boolean validUser = false;
            if (userId > 0) {
                try (PreparedStatement check = con.prepareStatement("SELECT 1 FROM \"user\" WHERE user_id = ?")) {
                    check.setInt(1, userId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) validUser = true;
                    }
                } catch (Exception e) {}
            }
            if (validUser) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<AuditEntry> getAllLogs() {
        List<AuditEntry> list = new ArrayList<>();
        String sql = """
                SELECT a.log_id, a.user_id, u.username, u.email, a.action, a.timestamp
                FROM audit_log a
                LEFT JOIN "user" u ON a.user_id = u.user_id
                ORDER BY a.timestamp DESC
                LIMIT 100
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AuditEntry entry = new AuditEntry();
                entry.setLogId(rs.getInt("log_id"));
                int uid = rs.getInt("user_id");
                if (!rs.wasNull()) entry.setUserId(uid);
                entry.setUsername(rs.getString("username"));
                entry.setEmail(rs.getString("email"));
                entry.setAction(rs.getString("action"));
                entry.setTimestamp(rs.getTimestamp("timestamp"));
                list.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<AuditEntry> getLogsByUser(int userId) {
        List<AuditEntry> list = new ArrayList<>();
        String sql = """
                SELECT a.log_id, a.user_id, u.username, a.action, a.timestamp
                FROM audit_log a
                LEFT JOIN "user" u ON a.user_id = u.user_id
                WHERE a.user_id = ?
                ORDER BY a.timestamp DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AuditEntry entry = new AuditEntry();
                entry.setLogId(rs.getInt("log_id"));
                entry.setUserId(userId);
                entry.setUsername(rs.getString("username"));
                entry.setAction(rs.getString("action"));
                entry.setTimestamp(rs.getTimestamp("timestamp"));
                list.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

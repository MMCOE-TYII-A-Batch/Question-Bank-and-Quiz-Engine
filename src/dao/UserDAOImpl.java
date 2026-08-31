package dao;

import admin.UserDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public int saveUser(UserDTO user, String roleName) {
        int generatedId = -1;
        String userSql = """
                INSERT INTO "user" (username, email, password_hash, status, division, department, contact_number)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING user_id;
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(userSql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash() != null ? user.getPasswordHash() : "Not Initialized");
            ps.setString(4, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            ps.setString(5, user.getDivision());
            ps.setString(6, user.getDepartment());
            ps.setString(7, user.getContactNumber());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                generatedId = rs.getInt("user_id");
                user.setUserId(generatedId);
            }
            rs.close();

            if (generatedId > 0 && roleName != null && !roleName.isEmpty()) {
                assignRole(con, generatedId, roleName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    private void assignRole(Connection con, int userId, String roleName) throws SQLException {
        String getRoleIdSql = "SELECT role_id FROM role WHERE LOWER(role_name) = LOWER(?)";
        int roleId = -1;
        try (PreparedStatement psRole = con.prepareStatement(getRoleIdSql)) {
            psRole.setString(1, roleName);
            ResultSet rs = psRole.executeQuery();
            if (rs.next()) {
                roleId = rs.getInt("role_id");
            }
            rs.close();
        }

        if (roleId == -1) {
            String insertRoleSql = "INSERT INTO role (role_name) VALUES (?) RETURNING role_id";
            try (PreparedStatement psIns = con.prepareStatement(insertRoleSql)) {
                psIns.setString(1, roleName);
                ResultSet rs = psIns.executeQuery();
                if (rs.next()) {
                    roleId = rs.getInt("role_id");
                }
                rs.close();
            }
        }

        if (roleId > 0) {
            String userRoleSql = "INSERT INTO user_role (user_id, role_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement psUserRole = con.prepareStatement(userRoleSql)) {
                psUserRole.setInt(1, userId);
                psUserRole.setInt(2, roleId);
                psUserRole.executeUpdate();
            }
        }
    }

    @Override
    public boolean updateUser(UserDTO user) {
        String sql = """
                UPDATE "user"
                SET username = ?, email = ?, division = ?, department = ?, contact_number = ?
                WHERE user_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getDivision());
            ps.setString(4, user.getDepartment());
            ps.setString(5, user.getContactNumber());
            ps.setInt(6, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE \"user\" SET status = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateUserRole(int userId, String roleName) {
        try (Connection con = DBConnection.getConnection()) {
            String delSql = "DELETE FROM user_role WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(delSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            assignRole(con, userId, roleName);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE \"user\" SET password_hash = ?, reset_token = NULL WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateResetToken(String email, String token, Timestamp expiry) {
        String sql = "UPDATE \"user\" SET reset_token = ?, reset_token_expiry = ? WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setTimestamp(2, expiry);
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean resetPasswordWithToken(String token, String newPasswordHash) {
        String sql = """
                UPDATE "user"
                SET password_hash = ?, reset_token = NULL, reset_token_expiry = NULL
                WHERE reset_token = ? AND reset_token_expiry > CURRENT_TIMESTAMP
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, token);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public UserDTO getUserById(int userId) {
        String sql = """
                SELECT u.*, r.role_name
                FROM "user" u
                LEFT JOIN user_role ur ON u.user_id = ur.user_id
                LEFT JOIN role r ON ur.role_id = r.role_id
                WHERE u.user_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        String sql = """
                SELECT u.*, r.role_name
                FROM "user" u
                LEFT JOIN user_role ur ON u.user_id = ur.user_id
                LEFT JOIN role r ON ur.role_id = r.role_id
                WHERE LOWER(u.email) = LOWER(?)
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        String sql = """
                SELECT u.*, r.role_name
                FROM "user" u
                LEFT JOIN user_role ur ON u.user_id = ur.user_id
                LEFT JOIN role r ON ur.role_id = r.role_id
                WHERE LOWER(u.username) = LOWER(?)
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UserDTO> getUsersByRole(String roleName) {
        List<UserDTO> list = new ArrayList<>();
        String sql = """
                SELECT u.*, r.role_name
                FROM "user" u
                JOIN user_role ur ON u.user_id = ur.user_id
                JOIN role r ON ur.role_id = r.role_id
                WHERE LOWER(r.role_name) = LOWER(?)
                ORDER BY u.user_id
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roleName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<UserDTO> getUsersByDivision(String division) {
        List<UserDTO> list = new ArrayList<>();
        String sql = """
                SELECT u.*, r.role_name
                FROM "user" u
                LEFT JOIN user_role ur ON u.user_id = ur.user_id
                LEFT JOIN role r ON ur.role_id = r.role_id
                WHERE u.division = ?
                ORDER BY u.user_id
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, division);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserDTO> list = new ArrayList<>();
        String sql = """
                SELECT u.*, r.role_name
                FROM "user" u
                LEFT JOIN user_role ur ON u.user_id = ur.user_id
                LEFT JOIN role r ON ur.role_id = r.role_id
                ORDER BY u.user_id
                """;
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean deleteUser(int userId) {
        String sql = "UPDATE \"user\" SET status = 'DELETED' WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateLastLogin(int userId) {
        String sql = "UPDATE \"user\" SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private UserDTO mapUser(ResultSet rs) throws SQLException {
        UserDTO u = new UserDTO();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setStatus(rs.getString("status"));
        u.setDivision(rs.getString("division"));
        u.setDepartment(rs.getString("department"));
        u.setContactNumber(rs.getString("contact_number"));
        u.setLastLogin(rs.getTimestamp("last_login"));
        u.setRoleName(rs.getString("role_name"));
        try { u.setSubject(rs.getString("subject")); } catch(SQLException ignored) {}
        try { u.setForcePasswordChange(rs.getBoolean("force_password_change")); } catch(SQLException ignored) {}
        return u;
    }

    @Override
    public boolean updateUserSubject(int userId, String subject) {
        String sql = "UPDATE \"user\" SET subject = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateForcePasswordChange(int userId, boolean forceChange) {
        String sql = "UPDATE \"user\" SET force_password_change = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, forceChange);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProfileFull(int userId, String email, String department, String contactNumber, String subject, String username) {
        String sql = "UPDATE \"user\" SET email = COALESCE(?, email), department = COALESCE(?, department), division = COALESCE(?, division), contact_number = COALESCE(?, contact_number), subject = COALESCE(?, subject), username = COALESCE(?, username) WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, department);
            ps.setString(3, department);
            ps.setString(4, contactNumber);
            ps.setString(5, subject);
            ps.setString(6, username);
            ps.setInt(7, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

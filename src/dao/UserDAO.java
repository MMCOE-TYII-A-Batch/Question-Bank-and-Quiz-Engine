package dao;

import admin.UserDTO;
import java.util.List;

public interface UserDAO {
    int saveUser(UserDTO user, String roleName);
    boolean updateUser(UserDTO user);
    boolean updateUserStatus(int userId, String status);
    boolean updateUserRole(int userId, String roleName);
    boolean updatePassword(int userId, String newPasswordHash);
    boolean updateResetToken(String email, String token, java.sql.Timestamp expiry);
    boolean resetPasswordWithToken(String token, String newPasswordHash);
    UserDTO getUserById(int userId);
    UserDTO getUserByEmail(String email);
    UserDTO getUserByUsername(String username);
    List<UserDTO> getUsersByRole(String roleName);
    List<UserDTO> getUsersByDivision(String division);
    List<UserDTO> getAllUsers();
    boolean deleteUser(int userId);
    void updateLastLogin(int userId);
    boolean updateUserSubject(int userId, String subject);
    boolean updateForcePasswordChange(int userId, boolean forceChange);
    boolean updateProfileFull(int userId, String email, String department, String contactNumber, String subject, String username);
}

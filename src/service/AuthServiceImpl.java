package service;

import admin.UserDTO;
import dao.UserDAO;
import dao.UserDAOImpl;
import dao.AuditLogDAO;
import dao.AuditLogDAOImpl;
import exception.ValidationException;
import util.PasswordUtil;

import java.sql.Timestamp;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    public AuthServiceImpl() {
        this.userDAO = new UserDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    @Override
    public UserDTO login(String identifier, String password) throws ValidationException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ValidationException("Username or Email is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password is required.");
        }

        String cleanId = identifier.trim();
        UserDTO user = cleanId.contains("@") ? userDAO.getUserByEmail(cleanId) : userDAO.getUserByUsername(cleanId);

        if (user == null) {
            throw new ValidationException("Invalid credentials.");
        }

        if ("DELETED".equalsIgnoreCase(user.getStatus()) || "INACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ValidationException("Account is inactive or deleted.");
        }

        String storedHash = user.getPasswordHash();

        // Support plain text checking for legacy initial setup fallback if hash matches directly or PasswordUtil matches
        boolean validPassword = false;
        if (storedHash != null) {
            if (PasswordUtil.verifyPassword(password, storedHash)) {
                validPassword = true;
            } else if (password.equals(storedHash)) {
                validPassword = true;
                // Auto-upgrade unhashed stored password to SHA-256
                userDAO.updatePassword(user.getUserId(), PasswordUtil.hashPassword(password));
            }
        }

        if (!validPassword) {
            auditLogDAO.logAction(user.getUserId(), "Failed login attempt for user: " + identifier);
            throw new ValidationException("Invalid username or password.");
        }

        userDAO.updateLastLogin(user.getUserId());
        auditLogDAO.logAction(user.getUserId(), "User logged in successfully: " + user.getUsername());
        return user;
    }

    @Override
    public boolean requestPasswordReset(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required.");
        }

        UserDTO user = userDAO.getUserByEmail(email.trim());
        if (user == null) {
            throw new ValidationException("No user found with email: " + email);
        }

        String token = UUID.randomUUID().toString();
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + (30 * 60 * 1000)); // 30 mins
        boolean updated = userDAO.updateResetToken(email.trim(), token, expiry);
        if (updated) {
            auditLogDAO.logAction(user.getUserId(), "Password reset requested. Token generated.");
        }
        return updated;
    }

    @Override
    public boolean resetPassword(String token, String newPassword) throws ValidationException {
        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException("Reset token is required.");
        }
        if (newPassword == null || newPassword.length() < 4) {
            throw new ValidationException("New password must be at least 4 characters long.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean success = userDAO.resetPasswordWithToken(token.trim(), newHash);
        if (!success) {
            throw new ValidationException("Invalid or expired password reset token.");
        }
        return true;
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        UserDTO user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ValidationException("User not found.");
        }

        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash()) && !oldPassword.equals(user.getPasswordHash())) {
            throw new ValidationException("Incorrect old password.");
        }

        if (newPassword == null || newPassword.length() < 4) {
            throw new ValidationException("New password must be at least 4 characters.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean updated = userDAO.updatePassword(userId, newHash);
        if (updated) {
            auditLogDAO.logAction(userId, "Changed password successfully.");
        }
        return updated;
    }

    @Override
    public boolean forceChangePassword(int userId, String newPassword) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        if (newPassword == null || newPassword.length() < 4) {
            throw new ValidationException("New password must be at least 4 characters.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean updated = userDAO.updatePassword(userId, newHash);
        if (updated) {
            userDAO.updateForcePasswordChange(userId, false);
            auditLogDAO.logAction(userId, "Completed mandatory initial password change.");
        }
        return updated;
    }

    @Override
    public UserDTO updateProfile(int userId, String email, String department, String contactNumber) throws ValidationException {
        return updateProfileFull(userId, email, department, contactNumber, null, null);
    }

    @Override
    public UserDTO updateProfileFull(int userId, String email, String department, String contactNumber, String subject, String username) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        UserDTO user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ValidationException("User not found.");
        }

        boolean updated = userDAO.updateProfileFull(userId, email, department, contactNumber, subject, username);
        if (!updated) {
            userDAO.updateUser(user);
        }
        auditLogDAO.logAction(userId, "Updated profile details.");
        return userDAO.getUserById(userId);
    }

    @Override
    public void logout(int userId) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        UserDTO user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ValidationException("User not found.");
        }
        auditLogDAO.logAction(userId, "LOGOUT");
    }
}

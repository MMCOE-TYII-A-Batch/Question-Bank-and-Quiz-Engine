package admin;

import dao.UserDAO;
import dao.UserDAOImpl;
import dao.AuditLogDAO;
import dao.AuditLogDAOImpl;
import exception.ValidationException;
import util.PasswordUtil;

import java.util.List;

public class AdminServiceImpl implements AdminService {

    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    public AdminServiceImpl() {
        this.userDAO = new UserDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    @Override
    public int addUser(UserDTO user, int adminUserId) throws ValidationException {
        if (user == null) {
            throw new ValidationException("User object cannot be null.");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username is required.");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required.");
        }
        if (userDAO.getUserByEmail(user.getEmail()) != null) {
            throw new ValidationException("Email already registered.");
        }

        // Default initial password setting if not provided
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty() || user.getPasswordHash().equals("Not Initialized")) {
            String role = user.getRoleName() != null ? user.getRoleName().toUpperCase() : "STUDENT";
            if (role.contains("TEACHER") || role.contains("INSTRUCTOR")) {
                user.setPasswordHash(PasswordUtil.hashPassword("mmcoe@123"));
            } else if (role.contains("ADMIN")) {
                user.setPasswordHash(PasswordUtil.hashPassword("mmcoeadmin@123"));
            } else {
                user.setPasswordHash(PasswordUtil.hashPassword(user.getUsername()));
            }
        } else {
            user.setPasswordHash(PasswordUtil.hashPassword(user.getPasswordHash()));
        }

        int userId = userDAO.saveUser(user, user.getRoleName());
        if (userId > 0) {
            auditLogDAO.logAction(adminUserId, "Created user: " + user.getUsername() + " (ID: " + userId + ")");
        }
        return userId;
    }

    @Override
    public boolean updateUserDesignation(int userId, String newRoleName, int adminUserId) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        if (newRoleName == null || newRoleName.trim().isEmpty()) {
            throw new ValidationException("Role name cannot be empty.");
        }

        boolean updated = userDAO.updateUserRole(userId, newRoleName);
        if (updated) {
            auditLogDAO.logAction(adminUserId, "Updated user ID " + userId + " role to " + newRoleName);
        }
        return updated;
    }

    @Override
    public boolean removeUser(int userId, int adminUserId) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        boolean deleted = userDAO.deleteUser(userId);
        if (deleted) {
            auditLogDAO.logAction(adminUserId, "Deactivated/Removed user ID " + userId);
        }
        return deleted;
    }

    @Override
    public boolean updateUserSubject(int userId, String subject, int adminUserId) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID.");
        }
        boolean updated = userDAO.updateUserSubject(userId, subject);
        if (updated) {
            auditLogDAO.logAction(adminUserId, "Updated subject for user ID " + userId + " to: " + subject);
        }
        return updated;
    }

    @Override
    public UserDTO getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    @Override
    public List<UserDTO> getUsersByRole(String roleName) {
        return userDAO.getUsersByRole(roleName);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userDAO.getAllUsers();
    }
}

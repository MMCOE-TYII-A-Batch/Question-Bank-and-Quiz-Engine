package service;

import admin.UserDTO;
import exception.ValidationException;

public interface AuthService {
    UserDTO login(String identifier, String password) throws ValidationException;
    boolean requestPasswordReset(String email) throws ValidationException;
    boolean resetPassword(String token, String newPassword) throws ValidationException;
    boolean changePassword(int userId, String oldPassword, String newPassword) throws ValidationException;
    boolean forceChangePassword(int userId, String newPassword) throws ValidationException;
    UserDTO updateProfile(int userId, String email, String department, String contactNumber) throws ValidationException;
    UserDTO updateProfileFull(int userId, String email, String department, String contactNumber, String subject, String username) throws ValidationException;
    void logout(int userId) throws ValidationException;
}

package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return null;
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return BCrypt.checkpw(plainPassword, storedHash);
        }
        // Legacy SHA-256 fallback check
        String shaHex = sha256Hex(plainPassword);
        if (shaHex.equalsIgnoreCase(storedHash) || plainPassword.equals(storedHash)) {
            return true;
        }
        return false;
    }

    public static String sha256Hex(String input) {
        if (input == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}

package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes

    public static class LockoutStatus {
        private final boolean locked;
        private final int attemptsLeft;
        private final long remainingSeconds;

        public LockoutStatus(boolean locked, int attemptsLeft, long remainingSeconds) {
            this.locked = locked;
            this.attemptsLeft = attemptsLeft;
            this.remainingSeconds = remainingSeconds;
        }

        public boolean isLocked() { return locked; }
        public int getAttemptsLeft() { return attemptsLeft; }
        public long getRemainingSeconds() { return remainingSeconds; }
    }

    private static class ClientAttemptRecord {
        int attempts = 0;
        long lockoutUntil = 0;
        long lastAttemptTime = System.currentTimeMillis();
    }

    private static final Map<String, ClientAttemptRecord> records = new ConcurrentHashMap<>();

    public static LockoutStatus checkStatus(String clientKey) {
        if (clientKey == null || clientKey.trim().isEmpty()) {
            return new LockoutStatus(false, MAX_ATTEMPTS, 0);
        }
        String key = clientKey.toLowerCase().trim();
        ClientAttemptRecord rec = records.get(key);
        if (rec == null) {
            return new LockoutStatus(false, MAX_ATTEMPTS, 0);
        }

        long now = System.currentTimeMillis();

        // Check if lockout expired
        if (rec.lockoutUntil > 0) {
            if (now >= rec.lockoutUntil) {
                // Lockout expired -> reset
                records.remove(key);
                return new LockoutStatus(false, MAX_ATTEMPTS, 0);
            } else {
                long remSecs = (rec.lockoutUntil - now + 999) / 1000;
                return new LockoutStatus(true, 0, remSecs);
            }
        }

        // If last attempt was more than 15 mins ago, reset attempts
        if (now - rec.lastAttemptTime > LOCKOUT_DURATION_MS) {
            records.remove(key);
            return new LockoutStatus(false, MAX_ATTEMPTS, 0);
        }

        int left = Math.max(0, MAX_ATTEMPTS - rec.attempts);
        return new LockoutStatus(false, left, 0);
    }

    public static LockoutStatus recordFailedAttempt(String clientKey) {
        if (clientKey == null || clientKey.trim().isEmpty()) {
            return new LockoutStatus(false, MAX_ATTEMPTS - 1, 0);
        }
        String key = clientKey.toLowerCase().trim();
        long now = System.currentTimeMillis();

        ClientAttemptRecord rec = records.computeIfAbsent(key, k -> new ClientAttemptRecord());

        // Reset if window expired
        if (now - rec.lastAttemptTime > LOCKOUT_DURATION_MS && rec.lockoutUntil == 0) {
            rec.attempts = 0;
        }

        rec.attempts++;
        rec.lastAttemptTime = now;

        if (rec.attempts >= MAX_ATTEMPTS) {
            rec.lockoutUntil = now + LOCKOUT_DURATION_MS;
            long remSecs = LOCKOUT_DURATION_MS / 1000;
            return new LockoutStatus(true, 0, remSecs);
        }

        int left = Math.max(0, MAX_ATTEMPTS - rec.attempts);
        return new LockoutStatus(false, left, 0);
    }

    public static void recordSuccessfulLogin(String clientKey) {
        if (clientKey != null) {
            records.remove(clientKey.toLowerCase().trim());
        }
    }

    public static void clearAll() {
        records.clear();
    }
}

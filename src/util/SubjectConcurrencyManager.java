package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SubjectConcurrencyManager {

    private static final Map<String, ReentrantLock> subjectLocks = new ConcurrentHashMap<>();

    public static ReentrantLock getLockForSubject(String subjectIdentifier) {
        if (subjectIdentifier == null || subjectIdentifier.trim().isEmpty()) {
            return new ReentrantLock();
        }
        String key = subjectIdentifier.toLowerCase().trim();
        return subjectLocks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }

    public static void executeWithLock(String subjectIdentifier, Runnable action) {
        ReentrantLock lock = getLockForSubject(subjectIdentifier);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
}

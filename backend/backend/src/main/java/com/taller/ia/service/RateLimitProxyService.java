package com.taller.ia.service;

import com.taller.ia.exception.RateLimitExceededException;
import com.taller.ia.model.PlanConfig;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Per-user fixed-window rate limiter (one-minute window).
 */
@Service
public class RateLimitProxyService {

    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Registers a request for the user or throws when the plan limit was reached.
     *
     * @throws RateLimitExceededException with the seconds to wait
     */
    public void checkAndRecord(String userId, PlanConfig plan) {
        long now = System.currentTimeMillis();
        Window window = windows.computeIfAbsent(userId, id -> new Window());
        long retryAfter = window.tryAcquire(plan.getRequestsPerMinute(), now);
        if (retryAfter > 0) {
            throw new RateLimitExceededException(retryAfter);
        }
    }

    /** Requests made by the user in the current window. */
    public int getCurrentCount(String userId) {
        Window window = windows.get(userId);
        return window == null ? 0 : window.currentCount(System.currentTimeMillis());
    }

    /** Seconds until the current window resets, or 0 when there is no active window. */
    public long getSecondsToReset(String userId) {
        Window window = windows.get(userId);
        return window == null ? 0L : window.secondsToReset(System.currentTimeMillis());
    }

    private static final class Window {

        private long windowStart;
        private int count;

        synchronized long tryAcquire(int limit, long now) {
            rollIfExpired(now);
            if (limit >= 0 && count >= limit) {
                return Math.max(1L, ceilSeconds(windowStart + WINDOW_MILLIS - now));
            }
            if (count == 0) {
                windowStart = now;
            }
            count++;
            return 0L;
        }

        synchronized int currentCount(long now) {
            rollIfExpired(now);
            return count;
        }

        synchronized long secondsToReset(long now) {
            rollIfExpired(now);
            if (count == 0) {
                return 0L;
            }
            return Math.max(0L, ceilSeconds(windowStart + WINDOW_MILLIS - now));
        }

        private void rollIfExpired(long now) {
            if (count > 0 && now - windowStart >= WINDOW_MILLIS) {
                count = 0;
            }
        }

        private static long ceilSeconds(long millis) {
            return (millis + 999L) / 1000L;
        }
    }
}

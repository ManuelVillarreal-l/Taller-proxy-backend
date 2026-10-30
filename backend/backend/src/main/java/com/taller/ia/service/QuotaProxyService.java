package com.taller.ia.service;

import com.taller.ia.exception.QuotaExceededException;
import com.taller.ia.model.DailyUsage;
import com.taller.ia.model.PlanConfig;
import com.taller.ia.model.QuotaSnapshot;
import com.taller.ia.model.UserQuota;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Per-user monthly token quota.
 */
@Service
public class QuotaProxyService {

    private final ConcurrentHashMap<String, UserQuota> quotas = new ConcurrentHashMap<>();
    private final long freeTokenLimit;

    public QuotaProxyService(@Value("${app.quota.free-token-limit:50000}") long freeTokenLimit) {
        this.freeTokenLimit = freeTokenLimit;
    }

    /** Returns the quota of the user, creating a FREE one on first access. */
    public UserQuota getOrCreate(String userId) {
        UserQuota quota = quotas.computeIfAbsent(userId, id -> new UserQuota(
                id, PlanConfig.FREE, limitFor(PlanConfig.FREE), nextResetDate(LocalDate.now())));
        synchronized (quota) {
            if (!LocalDate.now().isBefore(quota.getResetDate())) {
                quota.resetMonthly(nextResetDate(LocalDate.now()));
            }
        }
        return quota;
    }

    public QuotaSnapshot getSnapshot(String userId) {
        UserQuota quota = getOrCreate(userId);
        synchronized (quota) {
            return quota.toSnapshot();
        }
    }

    /**
     * Consumes tokens from the user's quota.
     *
     * @throws QuotaExceededException when the remaining tokens are not enough
     */
    public QuotaSnapshot checkAndConsume(String userId, long tokens) {
        UserQuota quota = getOrCreate(userId);
        synchronized (quota) {
            long limit = quota.getTokensLimit();
            if (limit >= 0 && quota.getTokensUsed() + tokens > limit) {
                throw new QuotaExceededException("Monthly token quota exhausted for user " + userId);
            }
            quota.addUsage(LocalDate.now(), tokens);
            return quota.toSnapshot();
        }
    }

    /** Upgrades FREE to PRO. Other plans are returned unchanged. */
    public QuotaSnapshot upgrade(String userId) {
        UserQuota quota = getOrCreate(userId);
        synchronized (quota) {
            if (quota.getPlan() == PlanConfig.FREE) {
                quota.setPlan(PlanConfig.PRO);
                quota.setTokensLimit(limitFor(PlanConfig.PRO));
            }
            return quota.toSnapshot();
        }
    }

    /** Token usage for the last {@code days} days, oldest first, including days without usage. */
    public List<DailyUsage> getHistory(String userId, int days) {
        UserQuota quota = getOrCreate(userId);
        LocalDate today = LocalDate.now();
        List<DailyUsage> history = new ArrayList<>(days);
        synchronized (quota) {
            for (int offset = days - 1; offset >= 0; offset--) {
                LocalDate date = today.minusDays(offset);
                history.add(new DailyUsage(date, quota.getDailyUsage().getOrDefault(date, 0L)));
            }
        }
        return history;
    }

    /** Starts a new billing period for every user. Invoked by the scheduler on day 1 of each month. */
    public void resetAllQuotas() {
        LocalDate newResetDate = nextResetDate(LocalDate.now());
        for (UserQuota quota : quotas.values()) {
            synchronized (quota) {
                quota.resetMonthly(newResetDate);
            }
        }
    }

    private long limitFor(PlanConfig plan) {
        return plan == PlanConfig.FREE ? freeTokenLimit : plan.getTokensPerMonth();
    }

    private static LocalDate nextResetDate(LocalDate from) {
        return from.withDayOfMonth(1).plusMonths(1);
    }
}

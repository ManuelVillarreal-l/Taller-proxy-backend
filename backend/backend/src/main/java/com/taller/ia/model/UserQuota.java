package com.taller.ia.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Mutable quota state for one user. Instances are guarded by the owning service,
 * which synchronizes on the instance before reading or writing.
 */
public class UserQuota {

    private static final int DAILY_USAGE_RETENTION_DAYS = 60;

    private final String userId;
    private PlanConfig plan;
    private long tokensUsed;
    private long tokensLimit;
    private LocalDate resetDate;
    private final Map<LocalDate, Long> dailyUsage = new TreeMap<>();

    public UserQuota(String userId, PlanConfig plan, long tokensLimit, LocalDate resetDate) {
        this.userId = userId;
        this.plan = plan;
        this.tokensLimit = tokensLimit;
        this.resetDate = resetDate;
    }

    public String getUserId() {
        return userId;
    }

    public PlanConfig getPlan() {
        return plan;
    }

    public void setPlan(PlanConfig plan) {
        this.plan = plan;
    }

    public long getTokensUsed() {
        return tokensUsed;
    }

    public long getTokensLimit() {
        return tokensLimit;
    }

    public void setTokensLimit(long tokensLimit) {
        this.tokensLimit = tokensLimit;
    }

    public LocalDate getResetDate() {
        return resetDate;
    }

    public Map<LocalDate, Long> getDailyUsage() {
        return Collections.unmodifiableMap(dailyUsage);
    }

    public void addUsage(LocalDate day, long tokens) {
        tokensUsed += tokens;
        dailyUsage.merge(day, tokens, Long::sum);
        LocalDate cutoff = day.minusDays(DAILY_USAGE_RETENTION_DAYS);
        dailyUsage.keySet().removeIf(date -> date.isBefore(cutoff));
    }

    /** Starts a new billing period. Daily usage is kept so the history chart stays populated. */
    public void resetMonthly(LocalDate newResetDate) {
        this.tokensUsed = 0L;
        this.resetDate = newResetDate;
    }

    public QuotaSnapshot toSnapshot() {
        return new QuotaSnapshot(plan, tokensUsed, tokensLimit, resetDate);
    }
}

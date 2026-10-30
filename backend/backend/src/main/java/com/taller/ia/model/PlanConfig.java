package com.taller.ia.model;

/**
 * Subscription plans. A value of -1 means "unlimited".
 */
public enum PlanConfig {

    FREE(10, 50_000L),
    PRO(60, 500_000L),
    ENTERPRISE(-1, -1L);

    public static final int UNLIMITED = -1;

    private final int requestsPerMinute;
    private final long tokensPerMonth;

    PlanConfig(int requestsPerMinute, long tokensPerMonth) {
        this.requestsPerMinute = requestsPerMinute;
        this.tokensPerMonth = tokensPerMonth;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getTokensPerMonth() {
        return tokensPerMonth;
    }
}

package com.taller.ia.model;

import java.time.LocalDate;

/**
 * Immutable view of a user's quota at a given moment.
 */
public record QuotaSnapshot(PlanConfig plan, long tokensUsed, long tokensLimit, LocalDate resetDate) {

    /** Remaining tokens, or -1 when the plan has unlimited tokens. */
    public long tokensRemaining() {
        if (tokensLimit < 0) {
            return -1L;
        }
        return Math.max(0L, tokensLimit - tokensUsed);
    }
}

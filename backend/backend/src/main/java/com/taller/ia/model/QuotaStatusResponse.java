package com.taller.ia.model;

import java.time.LocalDate;

public record QuotaStatusResponse(
        PlanConfig plan,
        long tokensUsed,
        long tokensLimit,
        long tokensRemaining,
        LocalDate resetDate,
        int reqPerMinUsed,
        int reqPerMinLimit,
        long secondsToReset) {
}

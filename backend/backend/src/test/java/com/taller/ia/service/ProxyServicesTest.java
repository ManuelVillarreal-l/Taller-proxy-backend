package com.taller.ia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taller.ia.exception.QuotaExceededException;
import com.taller.ia.exception.RateLimitExceededException;
import com.taller.ia.model.PlanConfig;
import org.junit.jupiter.api.Test;

class ProxyServicesTest {

    @Test
    void rateLimitBlocksRequestsAboveThePlanLimit() {
        RateLimitProxyService service = new RateLimitProxyService();
        for (int i = 0; i < 10; i++) {
            service.checkAndRecord("user", PlanConfig.FREE);
        }
        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> service.checkAndRecord("user", PlanConfig.FREE));
        assertTrue(ex.getRetryAfter() >= 1);
        assertEquals(10, service.getCurrentCount("user"));
    }

    @Test
    void enterprisePlanHasNoRequestLimit() {
        RateLimitProxyService service = new RateLimitProxyService();
        for (int i = 0; i < 200; i++) {
            service.checkAndRecord("user", PlanConfig.ENTERPRISE);
        }
        assertEquals(200, service.getCurrentCount("user"));
    }

    @Test
    void quotaThrowsWhenTokensAreExhausted() {
        QuotaProxyService service = new QuotaProxyService(100L);
        service.checkAndConsume("user", 60L);
        assertThrows(QuotaExceededException.class, () -> service.checkAndConsume("user", 60L));
    }

    @Test
    void upgradeMovesFreeUserToPro() {
        QuotaProxyService service = new QuotaProxyService(100L);
        assertEquals(PlanConfig.FREE, service.getSnapshot("user").plan());
        assertEquals(PlanConfig.PRO, service.upgrade("user").plan());
        assertEquals(500_000L, service.getSnapshot("user").tokensLimit());
    }
}

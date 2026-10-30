package com.taller.ia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuotaResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuotaResetScheduler.class);

    private final QuotaProxyService quotaProxyService;

    public QuotaResetScheduler(QuotaProxyService quotaProxyService) {
        this.quotaProxyService = quotaProxyService;
    }

    /** Runs at 00:00 on the first day of every month. */
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyQuotas() {
        log.info("Resetting monthly quotas");
        quotaProxyService.resetAllQuotas();
    }
}

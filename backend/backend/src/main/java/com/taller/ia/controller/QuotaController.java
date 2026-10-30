package com.taller.ia.controller;

import com.taller.ia.model.DailyUsage;
import com.taller.ia.model.PlanConfig;
import com.taller.ia.model.QuotaSnapshot;
import com.taller.ia.model.QuotaStatusResponse;
import com.taller.ia.model.UpgradeResponse;
import com.taller.ia.service.QuotaProxyService;
import com.taller.ia.service.RateLimitProxyService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private static final int HISTORY_DAYS = 7;

    private final QuotaProxyService quotaProxyService;
    private final RateLimitProxyService rateLimitProxyService;

    public QuotaController(QuotaProxyService quotaProxyService, RateLimitProxyService rateLimitProxyService) {
        this.quotaProxyService = quotaProxyService;
        this.rateLimitProxyService = rateLimitProxyService;
    }

    @GetMapping("/status")
    public QuotaStatusResponse status(@RequestParam(defaultValue = "demo-user") String userId) {
        QuotaSnapshot snapshot = quotaProxyService.getSnapshot(userId);
        PlanConfig plan = snapshot.plan();
        return new QuotaStatusResponse(
                plan,
                snapshot.tokensUsed(),
                snapshot.tokensLimit(),
                snapshot.tokensRemaining(),
                snapshot.resetDate(),
                rateLimitProxyService.getCurrentCount(userId),
                plan.getRequestsPerMinute(),
                rateLimitProxyService.getSecondsToReset(userId));
    }

    @GetMapping("/history")
    public List<DailyUsage> history(@RequestParam(defaultValue = "demo-user") String userId) {
        return quotaProxyService.getHistory(userId, HISTORY_DAYS);
    }

    @PostMapping("/upgrade")
    public UpgradeResponse upgrade(@RequestParam(defaultValue = "demo-user") String userId) {
        QuotaSnapshot snapshot = quotaProxyService.upgrade(userId);
        return new UpgradeResponse(snapshot.plan(), snapshot.tokensLimit(), "Plan updated to " + snapshot.plan());
    }
}

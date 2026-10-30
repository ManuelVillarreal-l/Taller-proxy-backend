package com.taller.ia.service;

import com.taller.ia.model.GenerationRequest;
import com.taller.ia.model.GenerationResponse;
import com.taller.ia.model.PlanConfig;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the proxy chain: rate limit -> quota -> AI generation.
 */
@Service
public class ProxyChainService {

    private final RateLimitProxyService rateLimitProxyService;
    private final QuotaProxyService quotaProxyService;
    private final AIGenerationService aiGenerationService;

    public ProxyChainService(RateLimitProxyService rateLimitProxyService,
                             QuotaProxyService quotaProxyService,
                             AIGenerationService aiGenerationService) {
        this.rateLimitProxyService = rateLimitProxyService;
        this.quotaProxyService = quotaProxyService;
        this.aiGenerationService = aiGenerationService;
    }

    public GenerationResponse process(String userId, GenerationRequest request) {
        PlanConfig plan = quotaProxyService.getSnapshot(userId).plan();

        // 1. Rate limit (throws RateLimitExceededException)
        rateLimitProxyService.checkAndRecord(userId, plan);

        // 2. Quota (throws QuotaExceededException)
        int estimatedTokens = aiGenerationService.estimateTokens(request.prompt());
        quotaProxyService.checkAndConsume(userId, estimatedTokens);

        // 3. AI generation
        return aiGenerationService.generate(request);
    }
}

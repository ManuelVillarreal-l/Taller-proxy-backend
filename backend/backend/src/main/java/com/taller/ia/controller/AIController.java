package com.taller.ia.controller;

import com.taller.ia.model.GenerationRequest;
import com.taller.ia.model.GenerationResponse;
import com.taller.ia.service.ProxyChainService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final String DEFAULT_USER_ID = "demo-user";

    private final ProxyChainService proxyChainService;

    public AIController(ProxyChainService proxyChainService) {
        this.proxyChainService = proxyChainService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(
            @Valid @RequestBody GenerationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = resolveUserId(headerUserId, request.userId());
        return ResponseEntity.ok(proxyChainService.process(userId, request));
    }

    private static String resolveUserId(String headerUserId, String bodyUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return headerUserId.trim();
        }
        if (bodyUserId != null && !bodyUserId.isBlank()) {
            return bodyUserId.trim();
        }
        return DEFAULT_USER_ID;
    }
}

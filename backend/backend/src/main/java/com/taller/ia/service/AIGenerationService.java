package com.taller.ia.service;

import com.taller.ia.model.GenerationRequest;
import com.taller.ia.model.GenerationResponse;

public interface AIGenerationService {

    GenerationResponse generate(GenerationRequest request);

    /** Rough token estimation: one token per four characters, at least one. */
    default int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }
}

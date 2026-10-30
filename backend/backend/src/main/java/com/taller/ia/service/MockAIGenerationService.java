package com.taller.ia.service;

import com.taller.ia.model.GenerationRequest;
import com.taller.ia.model.GenerationResponse;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

/**
 * Simulated AI provider: waits 1.2 seconds and returns a random canned text plus the prompt.
 */
@Service
public class MockAIGenerationService implements AIGenerationService {

    private static final long SIMULATED_LATENCY_MILLIS = 1200L;

    private static final List<String> RESPONSES = List.of(
            "Claro, aquí tienes una respuesta generada por el modelo simulado.",
            "Excelente pregunta. Analicemos el tema paso a paso para entenderlo mejor.",
            "Según el contexto proporcionado, la mejor estrategia es dividir el problema en partes más pequeñas.",
            "Esta es una respuesta de ejemplo creada por el servicio de IA de demostración.",
            "Para resolver esto, primero conviene definir los requisitos y luego validar cada supuesto.",
            "Interesante planteamiento. Considera evaluar alternativas antes de tomar una decisión final.");

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        try {
            Thread.sleep(SIMULATED_LATENCY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI generation was interrupted", e);
        }
        String randomText = RESPONSES.get(ThreadLocalRandom.current().nextInt(RESPONSES.size()));
        String text = randomText + "\n\nPrompt: " + request.prompt();
        return new GenerationResponse(text, estimateTokens(request.prompt()));
    }
}

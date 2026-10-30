package com.taller.ia.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerationRequest(
        @NotBlank(message = "Prompt must not be blank")
        @Size(max = 4000, message = "Prompt must not exceed 4000 characters")
        String prompt,
        String userId) {
}

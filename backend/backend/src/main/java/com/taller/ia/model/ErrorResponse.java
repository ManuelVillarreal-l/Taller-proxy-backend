package com.taller.ia.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, Long retryAfter) {

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error, null);
    }
}

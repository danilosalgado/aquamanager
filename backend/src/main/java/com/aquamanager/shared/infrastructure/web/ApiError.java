package com.aquamanager.shared.infrastructure.web;

import java.time.Instant;
import java.util.List;

/** Envelope padrão de resposta de erro da API. */
public record ApiError(
        String code,
        String message,
        List<FieldError> details,
        Instant timestamp,
        String path
) {
    public record FieldError(String field, String message) {
    }

    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, List.of(), Instant.now(), path);
    }

    public static ApiError of(String code, String message, List<FieldError> details, String path) {
        return new ApiError(code, message, details, Instant.now(), path);
    }
}

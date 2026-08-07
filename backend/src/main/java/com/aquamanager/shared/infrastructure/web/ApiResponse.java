package com.aquamanager.shared.infrastructure.web;

/** Envelope padrão de resposta de sucesso da API. */
public record ApiResponse<T>(T data) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}

package com.aquamanager.modules.auth.application.dto;

public record LoginResponse(String accessToken, long expiresIn, UsuarioResponse usuario) {
}

package com.aquamanager.modules.auth.application;

import com.aquamanager.modules.auth.domain.Usuario;

/** Resultado interno de login/registro/refresh — o refreshToken nunca é serializado na API (vai em cookie httpOnly). */
public record AuthenticationResult(String accessToken, long expiresIn, String refreshToken, Usuario usuario) {
}

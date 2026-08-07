package com.aquamanager.modules.auth.application.dto;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String role,
        boolean ativo,
        boolean emailConfirmado,
        boolean twoFactorEnabled,
        UUID empresaId
) {
}

package com.aquamanager.modules.admin.application.dto;

import java.util.UUID;

public record UsuarioAdminResponse(
        UUID id,
        String nome,
        String email,
        String role,
        boolean ativo,
        UUID empresaId,
        String empresaNome
) {
}

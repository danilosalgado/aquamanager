package com.aquamanager.modules.tenant.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        String nome,
        String documento,
        String endereco,
        String cidade,
        String estado,
        String telefone,
        String email,
        String status,
        Instant trialEndsAt,
        PlanoResponse plano
) {
}

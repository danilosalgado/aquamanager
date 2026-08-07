package com.aquamanager.modules.estoque.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovimentacaoResponse(
        UUID id,
        UUID itemId,
        String itemNome,
        String tipo,
        BigDecimal quantidade,
        String motivo,
        UUID usuarioId,
        Instant createdAt
) {
}

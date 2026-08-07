package com.aquamanager.modules.alerta.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AlertaResponse(
        UUID id,
        String tipo,
        String severidade,
        String titulo,
        String mensagem,
        String entidadeTipo,
        UUID entidadeId,
        boolean lido,
        Instant createdAt
) {
}

package com.aquamanager.modules.agenda.application.dto;

import com.aquamanager.modules.agenda.domain.TipoEvento;
import java.time.Instant;
import java.util.UUID;

public record EventoResponse(
        UUID id,
        TipoEvento tipo,
        String titulo,
        String descricao,
        Instant dataInicio,
        Instant dataFim,
        boolean concluido,
        Instant createdAt
) {
}

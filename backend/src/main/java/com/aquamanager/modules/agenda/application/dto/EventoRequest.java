package com.aquamanager.modules.agenda.application.dto;

import com.aquamanager.modules.agenda.domain.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record EventoRequest(
        @NotNull(message = "O tipo do evento é obrigatório")
        TipoEvento tipo,

        @NotBlank(message = "O título do evento é obrigatório")
        String titulo,

        String descricao,

        @NotNull(message = "A data de início é obrigatória")
        Instant dataInicio,

        Instant dataFim,

        boolean concluido
) {
}

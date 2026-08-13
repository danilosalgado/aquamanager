package com.aquamanager.modules.mortalidade.application.dto;

import com.aquamanager.modules.mortalidade.domain.CausaExclusao;
import java.time.LocalDate;
import java.util.UUID;

public record RegistroMortalidadeResponse(
        UUID id,
        UUID loteId,
        Integer quantidade,
        LocalDate data,
        CausaExclusao causa,
        String observacoes
) {
}

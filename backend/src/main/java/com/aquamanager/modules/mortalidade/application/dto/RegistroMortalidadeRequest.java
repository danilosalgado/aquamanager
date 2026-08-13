package com.aquamanager.modules.mortalidade.application.dto;

import com.aquamanager.modules.mortalidade.domain.CausaExclusao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;

public record RegistroMortalidadeRequest(
        @NotNull UUID loteId,
        @NotNull @Positive Integer quantidade,
        @NotNull LocalDate data,
        @NotNull CausaExclusao causa,
        String observacoes
) {
}

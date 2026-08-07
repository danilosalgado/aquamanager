package com.aquamanager.modules.mortalidade.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record RegistroMortalidadeRequest(
        @NotNull UUID loteId,
        @NotNull @Positive Integer quantidade,
        @NotNull LocalDate data,
        @NotBlank @Size(max = 150) String motivo,
        String observacoes
) {
}

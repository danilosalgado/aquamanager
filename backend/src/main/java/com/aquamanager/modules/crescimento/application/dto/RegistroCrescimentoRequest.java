package com.aquamanager.modules.crescimento.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegistroCrescimentoRequest(
        @NotNull UUID loteId,
        @NotNull @Positive BigDecimal pesoMedioG,
        @NotNull @Positive Integer quantidadeAmostra,
        @NotNull LocalDate dataPesagem
) {
}

package com.aquamanager.modules.alimentacao.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RegistroAlimentacaoRequest(
        @NotNull UUID loteId,
        @NotBlank @Size(max = 100) String tipoRacao,
        @Size(max = 150) String fornecedor,
        @NotNull @Positive BigDecimal quantidadeKg,
        @NotNull Instant horario,
        BigDecimal custo
) {
}

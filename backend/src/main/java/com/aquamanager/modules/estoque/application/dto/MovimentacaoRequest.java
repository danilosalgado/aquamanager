package com.aquamanager.modules.estoque.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record MovimentacaoRequest(
        @NotNull UUID itemId,
        @NotNull String tipo,
        @NotNull @Positive BigDecimal quantidade,
        @Size(max = 150) String motivo
) {
}

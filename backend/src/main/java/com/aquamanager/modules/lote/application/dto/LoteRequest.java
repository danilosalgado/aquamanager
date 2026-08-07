package com.aquamanager.modules.lote.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LoteRequest(
        @NotNull UUID tanqueId,
        @NotNull UUID especieId,
        @Size(max = 150) String fornecedor,
        @NotNull @Positive Integer quantidadeInicial,
        @NotNull BigDecimal pesoInicialG,
        BigDecimal valorCompra,
        @NotNull LocalDate dataCompra,
        LocalDate previsaoVenda,
        String status
) {
}

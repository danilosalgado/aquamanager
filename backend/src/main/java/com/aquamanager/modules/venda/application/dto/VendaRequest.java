package com.aquamanager.modules.venda.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VendaRequest(
        @NotNull UUID loteId,
        @NotBlank String categoriaProduto,
        @NotNull @Positive BigDecimal quantidadeKg,
        @NotNull @Positive BigDecimal valorTotal,
        @NotNull LocalDate dataVenda,
        UUID clienteId,
        String observacoes
) {
}

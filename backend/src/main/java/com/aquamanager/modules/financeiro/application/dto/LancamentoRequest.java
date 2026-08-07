package com.aquamanager.modules.financeiro.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoRequest(
        @NotNull String tipo,
        @NotBlank String categoria,
        @NotBlank String descricao,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate dataVencimento,
        String formaPagamento,
        UUID clienteId,
        UUID fornecedorId,
        UUID loteId
) {
}

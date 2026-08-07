package com.aquamanager.modules.financeiro.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoResponse(
        UUID id,
        String tipo,
        String categoria,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        String status,
        String formaPagamento,
        UUID clienteId,
        String clienteNome,
        UUID fornecedorId,
        String fornecedorNome,
        UUID loteId
) {
}

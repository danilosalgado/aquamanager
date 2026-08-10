package com.aquamanager.modules.venda.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VendaResponse(
        UUID id,
        UUID clienteId,
        String clienteNome,
        String categoriaProduto,
        BigDecimal quantidadeKg,
        BigDecimal valorTotal,
        LocalDate dataVenda,
        String observacoes
) {
}

package com.aquamanager.modules.estoque.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EstoqueItemResponse(
        UUID id,
        String categoria,
        String nome,
        String unidade,
        BigDecimal quantidadeAtual,
        BigDecimal quantidadeMinima,
        UUID fornecedorId,
        String fornecedorNome,
        LocalDate validade,
        BigDecimal precoUnitario
) {
}

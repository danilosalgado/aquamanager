package com.aquamanager.modules.lote.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LoteResponse(
        UUID id,
        UUID tanqueId,
        String tanqueNome,
        UUID especieId,
        String especieNome,
        String fornecedor,
        Integer quantidadeInicial,
        Integer quantidadeAtual,
        BigDecimal pesoInicialG,
        BigDecimal pesoAtualG,
        BigDecimal valorCompra,
        LocalDate dataCompra,
        LocalDate previsaoVenda,
        String status,
        BigDecimal biomassaAtualKg
) {
}

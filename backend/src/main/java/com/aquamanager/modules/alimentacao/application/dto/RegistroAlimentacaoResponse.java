package com.aquamanager.modules.alimentacao.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RegistroAlimentacaoResponse(
        UUID id,
        UUID loteId,
        String tipoRacao,
        String fornecedor,
        BigDecimal quantidadeKg,
        Instant horario,
        UUID usuarioId,
        BigDecimal custo
) {
}

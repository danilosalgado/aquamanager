package com.aquamanager.modules.dashboard.application.dto;

import java.math.BigDecimal;

/** Dados de produção por tanque para gráfico comparativo. */
public record ProducaoPorTanqueResponse(
        String tanqueNome,
        BigDecimal biomassaKg,
        long quantidadePeixes,
        Integer indiceSaude
) {
}

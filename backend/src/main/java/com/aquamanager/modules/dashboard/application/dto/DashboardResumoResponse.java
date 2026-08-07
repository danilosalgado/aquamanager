package com.aquamanager.modules.dashboard.application.dto;

import java.math.BigDecimal;

public record DashboardResumoResponse(
        long quantidadeTanques,
        long quantidadeLotesAtivos,
        long quantidadePeixes,
        BigDecimal biomassaTotalKg,
        BigDecimal pesoMedioG,
        BigDecimal conversaoAlimentarMedia,
        BigDecimal receita30Dias,
        BigDecimal despesa30Dias,
        BigDecimal lucro30Dias,
        long mortalidade30Dias,
        long alertasNaoLidos,
        Double indiceSaudeMedio
) {
}

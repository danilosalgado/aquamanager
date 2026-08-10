package com.aquamanager.modules.crescimento.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CrescimentoPotencialResponse(
        UUID loteId,
        String tanqueNome,
        String especieNome,
        BigDecimal pesoAtualG,
        BigDecimal taxaCrescimentoGDia,
        String confiabilidade,
        int pesagensConsideradas,
        List<ProjecaoPeso> projecoes
) {
    public record ProjecaoPeso(
            String rotulo,
            BigDecimal pesoAlvoG,
            boolean jaAtingido,
            Integer diasRestantes,
            LocalDate dataPrevista
    ) {
    }
}

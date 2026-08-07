package com.aquamanager.modules.tenant.application.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record PlanoResponse(
        UUID id,
        String codigo,
        String nome,
        BigDecimal precoMensal,
        Integer limiteTanques,
        Integer limiteUsuarios,
        Map<String, Boolean> recursos
) {
}

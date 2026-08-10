package com.aquamanager.modules.venda.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LucroBrutoPorTanqueResponse(
        UUID tanqueId,
        String tanqueNome,
        BigDecimal receita,
        BigDecimal custoRacao,
        BigDecimal custoOperacional,
        BigDecimal lucroBruto
) {
}

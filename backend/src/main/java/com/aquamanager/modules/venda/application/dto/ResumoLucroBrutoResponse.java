package com.aquamanager.modules.venda.application.dto;

import java.math.BigDecimal;

public record ResumoLucroBrutoResponse(
        BigDecimal receita,
        BigDecimal custoRacao,
        BigDecimal custoOperacional,
        BigDecimal lucroBruto
) {
}

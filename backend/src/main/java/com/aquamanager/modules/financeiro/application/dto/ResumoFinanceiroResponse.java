package com.aquamanager.modules.financeiro.application.dto;

import java.math.BigDecimal;

public record ResumoFinanceiroResponse(
        BigDecimal receitaTotal,
        BigDecimal despesaTotal,
        BigDecimal lucro,
        BigDecimal margemPercentual,
        BigDecimal receitaPendente,
        BigDecimal despesaPendente
) {
}

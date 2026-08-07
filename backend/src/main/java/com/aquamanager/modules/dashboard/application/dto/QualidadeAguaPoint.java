package com.aquamanager.modules.dashboard.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Ponto de qualidade da água com múltiplos parâmetros por data. */
public record QualidadeAguaPoint(
        LocalDate data,
        BigDecimal temperatura,
        BigDecimal ph,
        BigDecimal oxigenioDissolvido
) {
}

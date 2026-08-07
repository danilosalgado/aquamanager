package com.aquamanager.modules.dashboard.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Ponto de série temporal genérico com data e valor. */
public record TimeSeriesPoint(
        LocalDate data,
        BigDecimal valor
) {
}

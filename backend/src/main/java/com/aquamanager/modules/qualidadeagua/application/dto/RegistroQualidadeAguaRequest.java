package com.aquamanager.modules.qualidadeagua.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RegistroQualidadeAguaRequest(
        @NotNull UUID tanqueId,
        BigDecimal temperatura,
        BigDecimal ph,
        BigDecimal oxigenioDissolvido,
        BigDecimal amonia,
        BigDecimal nitrito,
        BigDecimal alcalinidade,
        BigDecimal salinidade,
        BigDecimal transparenciaCm,
        @NotNull Instant medidoEm
) {
}

package com.aquamanager.modules.qualidadeagua.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RegistroQualidadeAguaResponse(
        UUID id,
        UUID tanqueId,
        BigDecimal temperatura,
        BigDecimal ph,
        BigDecimal oxigenioDissolvido,
        BigDecimal amonia,
        BigDecimal nitrito,
        BigDecimal alcalinidade,
        BigDecimal salinidade,
        BigDecimal transparenciaCm,
        Instant medidoEm,
        UUID usuarioId
) {
}

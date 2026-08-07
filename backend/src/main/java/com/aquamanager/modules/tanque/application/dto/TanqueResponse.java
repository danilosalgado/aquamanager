package com.aquamanager.modules.tanque.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TanqueResponse(
        UUID id,
        String nome,
        String codigo,
        String tipo,
        BigDecimal areaM2,
        BigDecimal volumeM3,
        BigDecimal profundidadeM,
        BigDecimal capacidadeEstimadaKg,
        BigDecimal latitude,
        BigDecimal longitude,
        String status,
        String observacoes,
        List<String> fotos
) {
}

package com.aquamanager.modules.tanque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record TanqueRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 30) String codigo,
        @NotNull String tipo,
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

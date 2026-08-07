package com.aquamanager.modules.especie.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EspecieRequest(
        @NotBlank @Size(max = 80) String nome,
        @Size(max = 120) String nomeCientifico,
        Integer cicloDiasPadrao,
        BigDecimal pesoAbatePadraoG,
        BigDecimal tempMin,
        BigDecimal tempMax,
        BigDecimal phMin,
        BigDecimal phMax,
        BigDecimal oxigenioMin,
        BigDecimal amoniaMax,
        BigDecimal nitritoMax,
        Boolean ativo
) {
}

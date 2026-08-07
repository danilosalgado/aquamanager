package com.aquamanager.modules.especie.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EspecieResponse(
        UUID id,
        String nome,
        String nomeCientifico,
        Integer cicloDiasPadrao,
        BigDecimal pesoAbatePadraoG,
        BigDecimal tempMin,
        BigDecimal tempMax,
        BigDecimal phMin,
        BigDecimal phMax,
        BigDecimal oxigenioMin,
        BigDecimal amoniaMax,
        BigDecimal nitritoMax,
        boolean ativo,
        boolean global
) {
}

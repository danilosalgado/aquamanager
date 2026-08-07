package com.aquamanager.modules.saude.application.dto;

import java.util.List;
import java.util.UUID;

public record IndiceSaudeResponse(
        UUID tanqueId,
        Integer score,
        String classificacao,
        boolean semDadosSuficientes,
        List<String> detalhes
) {
}

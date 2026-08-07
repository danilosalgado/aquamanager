package com.aquamanager.modules.saude.application.dto;

import java.time.LocalDate;

public record IndiceSaudeHistoricoItem(LocalDate data, int score, String classificacao) {
}

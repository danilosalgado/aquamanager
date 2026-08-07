package com.aquamanager.modules.dashboard.application.dto;

import java.time.LocalDate;

public record SaudeMediaDiariaResponse(LocalDate data, double scoreMedio) {
}

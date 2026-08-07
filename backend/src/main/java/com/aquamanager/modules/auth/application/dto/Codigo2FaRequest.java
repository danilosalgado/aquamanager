package com.aquamanager.modules.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Codigo2FaRequest(@NotBlank @Pattern(regexp = "\\d{6}") String codigo) {
}

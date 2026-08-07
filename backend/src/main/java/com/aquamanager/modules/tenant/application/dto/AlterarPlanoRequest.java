package com.aquamanager.modules.tenant.application.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarPlanoRequest(@NotNull String codigoPlano) {
}

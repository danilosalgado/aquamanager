package com.aquamanager.modules.estoque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EstoqueItemRequest(
        @NotNull String categoria,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 10) String unidade,
        BigDecimal quantidadeMinima,
        UUID fornecedorId,
        LocalDate validade,
        BigDecimal precoUnitario
) {
}

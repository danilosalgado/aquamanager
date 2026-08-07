package com.aquamanager.modules.fornecedor.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 20) String documento,
        @Size(max = 20) String telefone,
        @Size(max = 150) String email,
        String produtosFornecidos,
        String observacoes
) {
}

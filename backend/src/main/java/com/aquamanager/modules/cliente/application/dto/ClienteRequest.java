package com.aquamanager.modules.cliente.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 20) String documento,
        @Size(max = 20) String telefone,
        @Size(max = 150) String email,
        @Size(max = 200) String endereco,
        String observacoes
) {
}

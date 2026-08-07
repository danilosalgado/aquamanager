package com.aquamanager.modules.tenant.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarEmpresaRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 200) String endereco,
        @Size(max = 100) String cidade,
        @Size(max = 2) String estado,
        @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 150) String email
) {
}

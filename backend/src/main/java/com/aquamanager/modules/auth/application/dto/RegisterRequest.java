package com.aquamanager.modules.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 150) String nomeEmpresa,
        @NotBlank @Size(min = 11, max = 20) String documento,
        String endereco,
        String cidade,
        @Size(max = 2) String estado,
        String telefone,
        @NotBlank @Email @Size(max = 150) String emailEmpresa,

        @NotBlank @Size(max = 120) String nomeUsuario,
        @NotBlank @Email @Size(max = 150) String emailUsuario,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "A senha deve conter letra maiúscula, minúscula e número.")
        String senha
) {
}

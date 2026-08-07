package com.aquamanager.modules.cliente.application.dto;

import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String documento,
        String telefone,
        String email,
        String endereco,
        String observacoes
) {
}

package com.aquamanager.modules.fornecedor.application.dto;

import java.util.UUID;

public record FornecedorResponse(
        UUID id,
        String nome,
        String documento,
        String telefone,
        String email,
        String produtosFornecidos,
        String observacoes
) {
}

package com.aquamanager.modules.mortalidade.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RegistroMortalidadeResponse(
        UUID id,
        UUID loteId,
        Integer quantidade,
        LocalDate data,
        String motivo,
        String observacoes
) {
}

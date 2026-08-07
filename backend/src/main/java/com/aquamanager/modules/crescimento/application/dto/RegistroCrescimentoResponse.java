package com.aquamanager.modules.crescimento.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegistroCrescimentoResponse(
        UUID id,
        UUID loteId,
        BigDecimal pesoMedioG,
        Integer quantidadeAmostra,
        BigDecimal biomassaKg,
        LocalDate dataPesagem,
        UUID usuarioId
) {
}

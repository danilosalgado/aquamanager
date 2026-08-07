package com.aquamanager.modules.tenant.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AssinaturaResponse(
        UUID id,
        String status,
        LocalDate dataInicio,
        LocalDate proximoVencimento,
        PlanoResponse plano
) {
}

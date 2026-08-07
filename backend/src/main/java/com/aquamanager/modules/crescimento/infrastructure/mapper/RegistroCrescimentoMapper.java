package com.aquamanager.modules.crescimento.infrastructure.mapper;

import com.aquamanager.modules.crescimento.application.dto.RegistroCrescimentoResponse;
import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import org.springframework.stereotype.Component;

@Component
public class RegistroCrescimentoMapper {

    public RegistroCrescimentoResponse toResponse(RegistroCrescimento registro) {
        return new RegistroCrescimentoResponse(
                registro.getId(),
                registro.getLote().getId(),
                registro.getPesoMedioG(),
                registro.getQuantidadeAmostra(),
                registro.getBiomassaKg(),
                registro.getDataPesagem(),
                registro.getUsuarioId()
        );
    }
}

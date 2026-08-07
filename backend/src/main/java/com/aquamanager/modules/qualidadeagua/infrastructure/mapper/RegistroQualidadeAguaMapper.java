package com.aquamanager.modules.qualidadeagua.infrastructure.mapper;

import com.aquamanager.modules.qualidadeagua.application.dto.RegistroQualidadeAguaResponse;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import org.springframework.stereotype.Component;

@Component
public class RegistroQualidadeAguaMapper {

    public RegistroQualidadeAguaResponse toResponse(RegistroQualidadeAgua registro) {
        return new RegistroQualidadeAguaResponse(
                registro.getId(),
                registro.getTanque().getId(),
                registro.getTemperatura(),
                registro.getPh(),
                registro.getOxigenioDissolvido(),
                registro.getAmonia(),
                registro.getNitrito(),
                registro.getAlcalinidade(),
                registro.getSalinidade(),
                registro.getTransparenciaCm(),
                registro.getMedidoEm(),
                registro.getUsuarioId()
        );
    }
}

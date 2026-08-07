package com.aquamanager.modules.mortalidade.infrastructure.mapper;

import com.aquamanager.modules.mortalidade.application.dto.RegistroMortalidadeResponse;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import org.springframework.stereotype.Component;

@Component
public class RegistroMortalidadeMapper {

    public RegistroMortalidadeResponse toResponse(RegistroMortalidade registro) {
        return new RegistroMortalidadeResponse(
                registro.getId(),
                registro.getLote().getId(),
                registro.getQuantidade(),
                registro.getData(),
                registro.getMotivo(),
                registro.getObservacoes()
        );
    }
}

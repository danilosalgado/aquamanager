package com.aquamanager.modules.alimentacao.infrastructure.mapper;

import com.aquamanager.modules.alimentacao.application.dto.RegistroAlimentacaoResponse;
import com.aquamanager.modules.alimentacao.domain.RegistroAlimentacao;
import org.springframework.stereotype.Component;

@Component
public class RegistroAlimentacaoMapper {

    public RegistroAlimentacaoResponse toResponse(RegistroAlimentacao registro) {
        return new RegistroAlimentacaoResponse(
                registro.getId(),
                registro.getLote().getId(),
                registro.getTipoRacao(),
                registro.getFornecedor(),
                registro.getQuantidadeKg(),
                registro.getHorario(),
                registro.getUsuarioId(),
                registro.getCusto()
        );
    }
}

package com.aquamanager.modules.alerta.infrastructure.mapper;

import com.aquamanager.modules.alerta.application.dto.AlertaResponse;
import com.aquamanager.modules.alerta.domain.Alerta;
import org.springframework.stereotype.Component;

@Component
public class AlertaMapper {

    public AlertaResponse toResponse(Alerta alerta) {
        return new AlertaResponse(
                alerta.getId(),
                alerta.getTipo().name(),
                alerta.getSeveridade().name(),
                alerta.getTitulo(),
                alerta.getMensagem(),
                alerta.getEntidadeTipo(),
                alerta.getEntidadeId(),
                alerta.isLido(),
                alerta.getCreatedAt()
        );
    }
}

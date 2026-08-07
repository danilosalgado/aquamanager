package com.aquamanager.modules.agenda.infrastructure.mapper;

import com.aquamanager.modules.agenda.application.dto.EventoRequest;
import com.aquamanager.modules.agenda.application.dto.EventoResponse;
import com.aquamanager.modules.agenda.domain.Evento;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    public Evento toEntity(EventoRequest request) {
        if (request == null) {
            return null;
        }
        Evento evento = new Evento();
        evento.setTipo(request.tipo());
        evento.setTitulo(request.titulo());
        evento.setDescricao(request.descricao());
        evento.setDataInicio(request.dataInicio());
        evento.setDataFim(request.dataFim());
        evento.setConcluido(request.concluido());
        return evento;
    }

    public void updateEntity(Evento evento, EventoRequest request) {
        if (request == null) {
            return;
        }
        evento.setTipo(request.tipo());
        evento.setTitulo(request.titulo());
        evento.setDescricao(request.descricao());
        evento.setDataInicio(request.dataInicio());
        evento.setDataFim(request.dataFim());
        evento.setConcluido(request.concluido());
    }

    public EventoResponse toResponse(Evento evento) {
        if (evento == null) {
            return null;
        }
        return new EventoResponse(
                evento.getId(),
                evento.getTipo(),
                evento.getTitulo(),
                evento.getDescricao(),
                evento.getDataInicio(),
                evento.getDataFim(),
                evento.isConcluido(),
                evento.getCreatedAt()
        );
    }
}

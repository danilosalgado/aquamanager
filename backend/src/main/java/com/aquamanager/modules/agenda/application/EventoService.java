package com.aquamanager.modules.agenda.application;

import com.aquamanager.modules.agenda.application.dto.EventoRequest;
import com.aquamanager.modules.agenda.application.dto.EventoResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventoService {

    EventoResponse criar(UUID empresaId, EventoRequest request);

    EventoResponse atualizar(UUID empresaId, UUID id, EventoRequest request);

    void remover(UUID empresaId, UUID id);

    EventoResponse buscarPorId(UUID empresaId, UUID id);

    Page<EventoResponse> listar(UUID empresaId, Pageable pageable);

    List<EventoResponse> listarPorPeriodo(UUID empresaId, Instant inicio, Instant fim);

    void alternarConcluido(UUID empresaId, UUID id);
}

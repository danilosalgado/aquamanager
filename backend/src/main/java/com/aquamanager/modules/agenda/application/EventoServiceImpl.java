package com.aquamanager.modules.agenda.application;

import com.aquamanager.modules.agenda.application.dto.EventoRequest;
import com.aquamanager.modules.agenda.application.dto.EventoResponse;
import com.aquamanager.modules.agenda.domain.Evento;
import com.aquamanager.modules.agenda.infrastructure.mapper.EventoMapper;
import com.aquamanager.modules.agenda.infrastructure.persistence.EventoRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;
    private final GoogleCalendarSyncService googleCalendarSyncService;

    @Override
    @Transactional
    public EventoResponse criar(UUID empresaId, EventoRequest request) {
        Evento evento = eventoMapper.toEntity(request);
        evento.setEmpresaId(empresaId);
        
        Evento salvo = eventoRepository.save(evento);
        
        // Sync to Google Calendar
        googleCalendarSyncService.syncEvent(salvo, com.aquamanager.shared.infrastructure.security.SecurityUtils.currentUserId());
        
        return eventoMapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public EventoResponse atualizar(UUID empresaId, UUID id, EventoRequest request) {
        Evento evento = getEvento(empresaId, id);
        eventoMapper.updateEntity(evento, request);
        
        Evento salvo = eventoRepository.save(evento);
        return eventoMapper.toResponse(salvo);
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID id) {
        Evento evento = getEvento(empresaId, id);
        eventoRepository.delete(evento);
    }

    @Override
    @Transactional(readOnly = true)
    public EventoResponse buscarPorId(UUID empresaId, UUID id) {
        return eventoMapper.toResponse(getEvento(empresaId, id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventoResponse> listar(UUID empresaId, Pageable pageable) {
        return eventoRepository.findByEmpresaId(empresaId, pageable)
                .map(eventoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorPeriodo(UUID empresaId, Instant inicio, Instant fim) {
        return eventoRepository.findByEmpresaIdAndDataInicioBetween(empresaId, inicio, fim)
                .stream()
                .map(eventoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void alternarConcluido(UUID empresaId, UUID id) {
        Evento evento = getEvento(empresaId, id);
        evento.setConcluido(!evento.isConcluido());
        eventoRepository.save(evento);
    }

    private Evento getEvento(UUID empresaId, UUID id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
        if (!evento.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Evento não encontrado");
        }
        return evento;
    }
}

package com.aquamanager.modules.agenda.infrastructure.persistence;

import com.aquamanager.modules.agenda.domain.Evento;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, UUID> {

    Page<Evento> findByEmpresaId(UUID empresaId, Pageable pageable);
    
    List<Evento> findByEmpresaIdAndDataInicioBetween(UUID empresaId, Instant inicio, Instant fim);
}

package com.aquamanager.modules.lote.infrastructure.persistence;

import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepository extends JpaRepository<Lote, UUID> {

    Page<Lote> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<Lote> findByEmpresaIdAndTanqueId(UUID empresaId, UUID tanqueId, Pageable pageable);

    Page<Lote> findByEmpresaIdAndStatus(UUID empresaId, StatusLote status, Pageable pageable);

    Page<Lote> findByEmpresaIdAndTanqueIdAndStatus(UUID empresaId, UUID tanqueId, StatusLote status, Pageable pageable);

    /** Usado futuramente pelo índice de saúde do tanque. */
    List<Lote> findByTanqueIdAndStatus(UUID tanqueId, StatusLote status);
}

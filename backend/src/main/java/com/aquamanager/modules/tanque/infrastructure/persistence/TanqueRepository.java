package com.aquamanager.modules.tanque.infrastructure.persistence;

import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TanqueRepository extends JpaRepository<Tanque, UUID> {
    Page<Tanque> findByEmpresaId(UUID empresaId, Pageable pageable);

    long countByEmpresaId(UUID empresaId);

    boolean existsByEmpresaIdAndCodigoIgnoreCase(UUID empresaId, String codigo);

    List<Tanque> findByEmpresaIdAndStatus(UUID empresaId, StatusTanque status);

    /** Usado pela importação em massa via planilha, que identifica o tanque pelo código. */
    Optional<Tanque> findByEmpresaIdAndCodigoIgnoreCase(UUID empresaId, String codigo);
}

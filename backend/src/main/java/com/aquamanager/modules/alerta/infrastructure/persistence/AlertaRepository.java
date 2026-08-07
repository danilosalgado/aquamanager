package com.aquamanager.modules.alerta.infrastructure.persistence;

import com.aquamanager.modules.alerta.domain.Alerta;
import com.aquamanager.modules.alerta.domain.TipoAlerta;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertaRepository extends JpaRepository<Alerta, UUID> {

    Page<Alerta> findByEmpresaIdOrderByCreatedAtDesc(UUID empresaId, Pageable pageable);

    Page<Alerta> findByEmpresaIdAndLidoOrderByCreatedAtDesc(UUID empresaId, boolean lido, Pageable pageable);

    long countByEmpresaIdAndLido(UUID empresaId, boolean lido);

    /** Deduplicação: evita recriar o mesmo alerta repetidamente dentro da janela de "cool-down". */
    boolean existsByEmpresaIdAndTipoAndEntidadeIdAndCreatedAtAfter(
            UUID empresaId, TipoAlerta tipo, UUID entidadeId, Instant desde);

    @Modifying
    @Query("UPDATE Alerta a SET a.lido = true WHERE a.empresaId = :empresaId AND a.lido = false")
    void marcarTodosComoLidos(@Param("empresaId") UUID empresaId);
}

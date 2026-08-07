package com.aquamanager.modules.estoque.infrastructure.persistence;

import com.aquamanager.modules.estoque.domain.EstoqueItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstoqueItemRepository extends JpaRepository<EstoqueItem, UUID> {

    Page<EstoqueItem> findByEmpresaId(UUID empresaId, Pageable pageable);

    List<EstoqueItem> findByEmpresaId(UUID empresaId);

    /** Usado pelo motor de alertas para identificar itens que precisam de reposição. */
    @Query("SELECT e FROM EstoqueItem e WHERE e.empresaId = :empresaId AND e.quantidadeAtual <= e.quantidadeMinima")
    List<EstoqueItem> findAbaixoDoMinimo(@Param("empresaId") UUID empresaId);
}

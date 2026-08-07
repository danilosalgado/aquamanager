package com.aquamanager.modules.estoque.infrastructure.persistence;

import com.aquamanager.modules.estoque.domain.EstoqueMovimentacao;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueMovimentacaoRepository extends JpaRepository<EstoqueMovimentacao, UUID> {

    Page<EstoqueMovimentacao> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<EstoqueMovimentacao> findByEmpresaIdAndItemId(UUID empresaId, UUID itemId, Pageable pageable);
}

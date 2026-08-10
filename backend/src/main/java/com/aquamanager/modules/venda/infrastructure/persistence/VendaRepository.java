package com.aquamanager.modules.venda.infrastructure.persistence;

import com.aquamanager.modules.venda.domain.Venda;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendaRepository extends JpaRepository<Venda, UUID> {

    Page<Venda> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<Venda> findByEmpresaIdAndCategoriaProdutoIgnoreCase(UUID empresaId, String categoriaProduto, Pageable pageable);

    /** Usado pelo relatório de lucro bruto por tanque — agregação feita em memória. */
    List<Venda> findByEmpresaId(UUID empresaId);

    @Query("SELECT DISTINCT v.categoriaProduto FROM Venda v WHERE v.empresaId = :empresaId ORDER BY v.categoriaProduto")
    List<String> listarCategoriasDistintas(@Param("empresaId") UUID empresaId);
}

package com.aquamanager.modules.fornecedor.infrastructure.persistence;

import com.aquamanager.modules.fornecedor.domain.Fornecedor;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, UUID> {
    Page<Fornecedor> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<Fornecedor> findByEmpresaIdAndNomeContainingIgnoreCase(UUID empresaId, String nome, Pageable pageable);
}

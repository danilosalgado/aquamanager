package com.aquamanager.modules.cliente.infrastructure.persistence;

import com.aquamanager.modules.cliente.domain.Cliente;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Page<Cliente> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<Cliente> findByEmpresaIdAndNomeContainingIgnoreCase(UUID empresaId, String nome, Pageable pageable);
}

package com.aquamanager.modules.tenant.infrastructure.persistence;

import com.aquamanager.modules.tenant.domain.Empresa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    boolean existsByDocumento(String documento);

    Optional<Empresa> findByDocumento(String documento);
}

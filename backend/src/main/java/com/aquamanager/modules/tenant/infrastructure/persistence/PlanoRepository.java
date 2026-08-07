package com.aquamanager.modules.tenant.infrastructure.persistence;

import com.aquamanager.modules.tenant.domain.Plano;
import com.aquamanager.modules.tenant.domain.PlanoCodigo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoRepository extends JpaRepository<Plano, UUID> {
    Optional<Plano> findByCodigo(PlanoCodigo codigo);
}

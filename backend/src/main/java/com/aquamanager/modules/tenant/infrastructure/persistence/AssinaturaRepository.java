package com.aquamanager.modules.tenant.infrastructure.persistence;

import com.aquamanager.modules.tenant.domain.Assinatura;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssinaturaRepository extends JpaRepository<Assinatura, UUID> {
    Optional<Assinatura> findFirstByEmpresaIdOrderByCreatedAtDesc(UUID empresaId);

    Optional<Assinatura> findByGatewaySubscriptionId(String gatewaySubscriptionId);
}

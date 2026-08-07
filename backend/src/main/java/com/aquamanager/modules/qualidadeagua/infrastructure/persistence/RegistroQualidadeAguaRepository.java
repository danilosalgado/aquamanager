package com.aquamanager.modules.qualidadeagua.infrastructure.persistence;

import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroQualidadeAguaRepository extends JpaRepository<RegistroQualidadeAgua, UUID> {

    Page<RegistroQualidadeAgua> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<RegistroQualidadeAgua> findByEmpresaIdAndTanqueId(UUID empresaId, UUID tanqueId, Pageable pageable);

    List<RegistroQualidadeAgua> findByTanqueIdAndMedidoEmBetween(UUID tanqueId, Instant inicio, Instant fim);

    Optional<RegistroQualidadeAgua> findFirstByTanqueIdOrderByMedidoEmDesc(UUID tanqueId);
}

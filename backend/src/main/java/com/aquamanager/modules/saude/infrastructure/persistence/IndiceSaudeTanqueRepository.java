package com.aquamanager.modules.saude.infrastructure.persistence;

import com.aquamanager.modules.saude.domain.IndiceSaudeTanque;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndiceSaudeTanqueRepository extends JpaRepository<IndiceSaudeTanque, UUID> {

    List<IndiceSaudeTanque> findByEmpresaIdAndTanqueIdAndDataBetweenOrderByDataAsc(
            UUID empresaId, UUID tanqueId, LocalDate inicio, LocalDate fim);

    Optional<IndiceSaudeTanque> findByTanqueIdAndData(UUID tanqueId, LocalDate data);

    /** Usado pelo dashboard para a média diária (todos os tanques) — agregação feita em memória. */
    List<IndiceSaudeTanque> findByEmpresaIdAndDataBetweenOrderByDataAsc(UUID empresaId, LocalDate inicio, LocalDate fim);
}

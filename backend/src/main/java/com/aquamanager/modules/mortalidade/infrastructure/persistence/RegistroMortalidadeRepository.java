package com.aquamanager.modules.mortalidade.infrastructure.persistence;

import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroMortalidadeRepository extends JpaRepository<RegistroMortalidade, UUID> {

    Page<RegistroMortalidade> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<RegistroMortalidade> findByEmpresaIdAndLoteId(UUID empresaId, UUID loteId, Pageable pageable);

    List<RegistroMortalidade> findByEmpresaIdAndDataBetween(UUID empresaId, LocalDate inicio, LocalDate fim);

    /** Usado pelo motor de alertas para detectar mortalidade elevada recente. */
    List<RegistroMortalidade> findByLoteIdAndDataGreaterThanEqual(UUID loteId, LocalDate data);
}

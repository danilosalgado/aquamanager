package com.aquamanager.modules.alimentacao.infrastructure.persistence;

import com.aquamanager.modules.alimentacao.domain.RegistroAlimentacao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroAlimentacaoRepository extends JpaRepository<RegistroAlimentacao, UUID> {

    Page<RegistroAlimentacao> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<RegistroAlimentacao> findByEmpresaIdAndLoteId(UUID empresaId, UUID loteId, Pageable pageable);

    List<RegistroAlimentacao> findByLoteIdAndHorarioBetween(UUID loteId, Instant inicio, Instant fim);
}

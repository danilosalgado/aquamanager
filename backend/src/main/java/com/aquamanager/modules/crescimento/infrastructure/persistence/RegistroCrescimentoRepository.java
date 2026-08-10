package com.aquamanager.modules.crescimento.infrastructure.persistence;

import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroCrescimentoRepository extends JpaRepository<RegistroCrescimento, UUID> {

    Page<RegistroCrescimento> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<RegistroCrescimento> findByEmpresaIdAndLoteId(UUID empresaId, UUID loteId, Pageable pageable);

    Optional<RegistroCrescimento> findFirstByLoteIdOrderByDataPesagemDesc(UUID loteId);

    /** Usado pelo índice de saúde para comparar a última pesagem com a anterior. */
    List<RegistroCrescimento> findTop2ByLoteIdOrderByDataPesagemDesc(UUID loteId);

    /** Usado pela projeção de crescimento potencial (regressão sobre o histórico completo). */
    List<RegistroCrescimento> findByLoteIdOrderByDataPesagemAsc(UUID loteId);
}

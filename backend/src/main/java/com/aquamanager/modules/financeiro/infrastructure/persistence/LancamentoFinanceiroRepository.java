package com.aquamanager.modules.financeiro.infrastructure.persistence;

import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import com.aquamanager.modules.financeiro.domain.StatusLancamento;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, UUID> {

    Page<LancamentoFinanceiro> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<LancamentoFinanceiro> findByEmpresaIdAndTipo(UUID empresaId, TipoLancamento tipo, Pageable pageable);

    Page<LancamentoFinanceiro> findByEmpresaIdAndStatus(UUID empresaId, StatusLancamento status, Pageable pageable);

    List<LancamentoFinanceiro> findByEmpresaIdAndDataVencimentoBetween(UUID empresaId, LocalDate inicio, LocalDate fim);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l "
            + "WHERE l.empresaId = :empresaId AND l.tipo = :tipo AND l.status = 'PAGO' "
            + "AND l.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal somarPagoPorTipoEPeriodo(@Param("empresaId") UUID empresaId, @Param("tipo") TipoLancamento tipo,
                                        @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l "
            + "WHERE l.empresaId = :empresaId AND l.tipo = :tipo AND l.status IN ('PENDENTE', 'ATRASADO')")
    BigDecimal somarPendentePorTipo(@Param("empresaId") UUID empresaId, @Param("tipo") TipoLancamento tipo);

    /** Job de sistema: promove lançamentos vencidos e ainda pendentes para ATRASADO, em todos os tenants. */
    @Modifying
    @Query("UPDATE LancamentoFinanceiro l SET l.status = 'ATRASADO' "
            + "WHERE l.status = 'PENDENTE' AND l.dataVencimento < :hoje")
    int marcarVencidosComoAtrasado(@Param("hoje") LocalDate hoje);
}

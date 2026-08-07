package com.aquamanager.modules.financeiro.application;

import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.shared.infrastructure.persistence.TenantSessionManager;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Promove lançamentos pendentes vencidos para ATRASADO — insumo do motor de alertas. */
@Component
@RequiredArgsConstructor
public class FinanceiroScheduler {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroScheduler.class);

    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final TenantSessionManager tenantSessionManager;

    @Scheduled(cron = "0 30 2 * * *") // 02:30 todos os dias
    @Transactional
    public void marcarLancamentosAtrasados() {
        tenantSessionManager.runAsSystem(() -> {
            int atualizados = lancamentoRepository.marcarVencidosComoAtrasado(LocalDate.now());
            if (atualizados > 0) {
                log.info("{} lançamento(s) financeiro(s) marcados como ATRASADO.", atualizados);
            }
        });
    }
}

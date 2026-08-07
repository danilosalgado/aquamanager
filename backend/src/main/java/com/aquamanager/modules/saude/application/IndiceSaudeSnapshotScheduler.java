package com.aquamanager.modules.saude.application;

import com.aquamanager.modules.saude.domain.Classificacao;
import com.aquamanager.modules.saude.domain.IndiceSaudeTanque;
import com.aquamanager.modules.saude.infrastructure.persistence.IndiceSaudeTanqueRepository;
import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.modules.tenant.domain.EmpresaStatus;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.shared.infrastructure.persistence.TenantContext;
import com.aquamanager.shared.infrastructure.persistence.TenantSessionManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Gera diariamente um snapshot do índice de saúde de cada tanque ativo, para a série histórica do dashboard. */
@Component
@RequiredArgsConstructor
public class IndiceSaudeSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(IndiceSaudeSnapshotScheduler.class);

    private final EmpresaRepository empresaRepository;
    private final TenantSessionManager tenantSessionManager;
    private final TanqueRepository tanqueRepository;
    private final IndiceSaudeServiceImpl indiceSaudeService;
    private final IndiceSaudeTanqueRepository indiceSaudeRepository;

    @Scheduled(cron = "0 0 4 * * *") // 04:00 todos os dias
    public void gerarSnapshots() {
        List<UUID> empresaIds = tenantSessionManager.runAsSystem(() -> empresaRepository.findAll().stream()
                .filter(e -> e.getStatus() != EmpresaStatus.CANCELADA && e.getStatus() != EmpresaStatus.BLOQUEADA)
                .map(e -> e.getId())
                .toList());

        for (UUID empresaId : empresaIds) {
            TenantContext.setTenantId(empresaId);
            try {
                processarEmpresa(empresaId);
            } catch (Exception ex) {
                log.error("Falha ao gerar snapshot de índice de saúde da empresa {}", empresaId, ex);
            } finally {
                TenantContext.clear();
            }
        }
    }

    @Transactional
    void processarEmpresa(UUID empresaId) {
        for (Tanque tanque : tanqueRepository.findByEmpresaIdAndStatus(empresaId, StatusTanque.ATIVO)) {
            var resultado = indiceSaudeService.calcular(tanque.getId());
            if (resultado.semDadosSuficientes() || resultado.score() == null) {
                continue;
            }

            LocalDate hoje = LocalDate.now();
            IndiceSaudeTanque snapshot = indiceSaudeRepository.findByTanqueIdAndData(tanque.getId(), hoje)
                    .orElseGet(IndiceSaudeTanque::new);
            snapshot.setEmpresaId(empresaId);
            snapshot.setTanque(tanque);
            snapshot.setData(hoje);
            snapshot.setScore(resultado.score());
            snapshot.setClassificacao(Classificacao.valueOf(resultado.classificacao()));
            indiceSaudeRepository.save(snapshot);
        }
    }
}

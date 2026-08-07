package com.aquamanager.modules.alerta.application;

import com.aquamanager.modules.tenant.domain.EmpresaStatus;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.shared.infrastructure.persistence.TenantContext;
import com.aquamanager.shared.infrastructure.persistence.TenantSessionManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Ponto de entrada do motor de alertas: obtém a lista de empresas ativas (bypass de
 * RLS, somente para a leitura da lista de IDs) e processa cada uma em sua própria
 * transação devidamente isolada por tenant — uma falha numa empresa não afeta as demais.
 */
@Component
@RequiredArgsConstructor
public class AlertScanScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScanScheduler.class);

    private final EmpresaRepository empresaRepository;
    private final TenantSessionManager tenantSessionManager;
    private final AlertEngineService alertEngineService;

    @Scheduled(fixedRate = 30 * 60 * 1000) // a cada 30 minutos
    public void executarVarredura() {
        List<UUID> empresaIds = tenantSessionManager.runAsSystem(() -> empresaRepository.findAll().stream()
                .filter(e -> e.getStatus() != EmpresaStatus.CANCELADA && e.getStatus() != EmpresaStatus.BLOQUEADA)
                .map(e -> e.getId())
                .toList());

        for (UUID empresaId : empresaIds) {
            TenantContext.setTenantId(empresaId);
            try {
                alertEngineService.processarEmpresa(empresaId);
            } catch (Exception ex) {
                log.error("Falha ao processar alertas da empresa {}", empresaId, ex);
            } finally {
                TenantContext.clear();
            }
        }
    }
}

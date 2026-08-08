package com.aquamanager.modules.tenant.application;

import com.aquamanager.modules.tenant.domain.EmpresaStatus;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.shared.infrastructure.persistence.TenantSessionManager;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Bloqueia automaticamente empresas cujo período de trial expirou sem assinatura ativa. */
@Component
@RequiredArgsConstructor
public class TrialExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrialExpirationScheduler.class);

    private final EmpresaRepository empresaRepository;
    private final TenantSessionManager tenantSessionManager;

    @Scheduled(cron = "0 0 3 * * *") // 03:00 todos os dias
    @Transactional
    public void bloquearTrialsExpirados() {
        tenantSessionManager.runAsSystem(() -> {
            Instant now = Instant.now();
            var expiradas = empresaRepository.findAll().stream()
                    .filter(e -> !e.isIsentoCobranca())
                    .filter(e -> e.getStatus() == EmpresaStatus.TRIAL && e.getTrialEndsAt().isBefore(now))
                    .toList();

            expiradas.forEach(empresa -> {
                empresa.setStatus(EmpresaStatus.BLOQUEADA);
                log.info("Trial expirado: empresa {} bloqueada.", empresa.getId());
            });
            empresaRepository.saveAll(expiradas);
        });
    }
}

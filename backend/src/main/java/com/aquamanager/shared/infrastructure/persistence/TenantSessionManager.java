package com.aquamanager.shared.infrastructure.persistence;

import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import java.util.function.Supplier;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Ativa o isolamento multi-tenant (filtro Hibernate + variável de sessão do
 * PostgreSQL para RLS) na conexão/persistence-context da transação atual.
 *
 * Chamado automaticamente por {@link TenantRlsAspect} no início de toda transação
 * onde {@link TenantContext} já possui um tenant. Também pode ser chamado
 * manualmente para o caso especial de bootstrap do cadastro de uma nova empresa,
 * onde o tenant só passa a existir NO MEIO da transação (ver módulo auth).
 */
@Component
public class TenantSessionManager {

    @PersistenceContext
    private EntityManager entityManager;

    public void activate(UUID tenantId) {
        if (tenantId == null) {
            return;
        }
        entityManager.createNativeQuery("SELECT set_config('app.tenant_id', :tenantId, true)")
                .setParameter("tenantId", tenantId.toString())
                .getSingleResult();

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter(TenantAwareEntity.TENANT_FILTER_NAME)
                .setParameter(TenantAwareEntity.TENANT_PARAM_NAME, tenantId);
    }

    /**
     * Executa {@code action} com o isolamento multi-tenant explicitamente
     * desativado (bypass de RLS + filtro Hibernate desabilitado), para jobs
     * internos legitimamente cross-tenant (ex.: expiração de trial). Nunca deve
     * ser alcançável a partir de uma requisição HTTP autenticada.
     */
    public <T> T runAsSystem(Supplier<T> action) {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter(TenantAwareEntity.TENANT_FILTER_NAME);
        entityManager.createNativeQuery("SELECT set_config('app.bypass_tenant_check', 'true', true)")
                .getSingleResult();
        try {
            return action.get();
        } finally {
            entityManager.createNativeQuery("SELECT set_config('app.bypass_tenant_check', 'false', true)")
                    .getSingleResult();
        }
    }

    public void runAsSystem(Runnable action) {
        runAsSystem(() -> {
            action.run();
            return null;
        });
    }
}

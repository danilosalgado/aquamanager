package com.aquamanager.shared.infrastructure.persistence;

import java.util.UUID;

/**
 * Contexto do tenant (empresa) da requisição atual, propagado por {@link ThreadLocal}.
 * Preenchido por {@code JwtAuthenticationFilter} após validar o token e limpo ao final
 * de cada requisição.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}

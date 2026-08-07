package com.aquamanager.shared.domain;

import com.aquamanager.shared.infrastructure.persistence.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * Superclasse para todas as entidades pertencentes a uma empresa (tenant).
 *
 * Isolamento multi-tenant em duas camadas independentes:
 *  1) Filtro Hibernate ({@link #TENANT_FILTER_NAME}) habilitado por transação em
 *     {@link com.aquamanager.shared.infrastructure.persistence.TenantRlsAspect}.
 *  2) Row-Level Security do PostgreSQL (policies criadas via Flyway), usando a
 *     mesma variável de sessão setada pelo aspecto acima.
 *
 * Mesmo que uma consulta esqueça de aplicar o filtro do Hibernate, o RLS do banco
 * impede o vazamento de dados entre empresas.
 */
@Getter
@Setter
@MappedSuperclass
@FilterDef(name = TenantAwareEntity.TENANT_FILTER_NAME,
        parameters = @ParamDef(name = TenantAwareEntity.TENANT_PARAM_NAME, type = UUID.class))
@Filter(name = TenantAwareEntity.TENANT_FILTER_NAME,
        condition = "empresa_id = :" + TenantAwareEntity.TENANT_PARAM_NAME)
public abstract class TenantAwareEntity extends BaseEntity {

    public static final String TENANT_FILTER_NAME = "tenantFilter";
    public static final String TENANT_PARAM_NAME = "tenantId";

    @Column(name = "empresa_id", nullable = false, updatable = false)
    private UUID empresaId;

    @PrePersist
    protected void assignTenantIfMissing() {
        if (empresaId == null) {
            empresaId = TenantContext.getTenantId();
        }
    }
}

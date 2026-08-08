package com.aquamanager.modules.tenant.domain;

import com.aquamanager.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tenant raiz da plataforma. Note que Empresa NÃO estende {@code TenantAwareEntity}:
 * ela É o tenant (identificada pelo próprio id), não pertence a um.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empresas")
public class Empresa extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(length = 200)
    private String endereco;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmpresaStatus status = EmpresaStatus.TRIAL;

    @Column(name = "trial_ends_at", nullable = false)
    private Instant trialEndsAt;

    /**
     * Contas administrativas da plataforma (não clientes): não entram no trial, não
     * podem gerar checkout/cobrança e ficam de fora do bloqueio automático por trial
     * expirado.
     */
    @Column(name = "isento_cobranca", nullable = false)
    private boolean isentoCobranca = false;
}

package com.aquamanager.modules.auth.domain;

import com.aquamanager.shared.domain.Role;
import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela de fronteira de autenticação: consultada por e-mail antes de o tenant
 * ser conhecido (login). Por isso NÃO recebe policy de Row-Level Security
 * (ver V18__row_level_security.sql), embora estenda TenantAwareEntity para se
 * beneficiar do filtro Hibernate em consultas pós-login (ex.: listar usuários
 * da empresa).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario extends TenantAwareEntity {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "email_confirmado", nullable = false)
    private boolean emailConfirmado = false;

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 64)
    private String twoFactorSecret;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "google_refresh_token", length = 255)
    private String googleRefreshToken;

    public boolean estaBloqueado() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }
}

package com.aquamanager.modules.auth.domain;

import com.aquamanager.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "login_logs")
public class LoginLog extends BaseEntity {

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(name = "email_tentativa", nullable = false, length = 150)
    private String emailTentativa;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false)
    private boolean sucesso;

    @Column(name = "motivo_falha", length = 100)
    private String motivoFalha;
}

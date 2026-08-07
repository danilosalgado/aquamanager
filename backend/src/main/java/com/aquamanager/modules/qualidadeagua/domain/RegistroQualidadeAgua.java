package com.aquamanager.modules.qualidadeagua.domain;

import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "registros_qualidade_agua")
public class RegistroQualidadeAgua extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tanque_id", nullable = false)
    private Tanque tanque;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperatura;

    @Column(precision = 4, scale = 2)
    private BigDecimal ph;

    @Column(name = "oxigenio_dissolvido", precision = 5, scale = 2)
    private BigDecimal oxigenioDissolvido;

    @Column(precision = 6, scale = 3)
    private BigDecimal amonia;

    @Column(precision = 6, scale = 3)
    private BigDecimal nitrito;

    @Column(precision = 6, scale = 2)
    private BigDecimal alcalinidade;

    @Column(precision = 5, scale = 2)
    private BigDecimal salinidade;

    @Column(name = "transparencia_cm", precision = 6, scale = 2)
    private BigDecimal transparenciaCm;

    @Column(name = "medido_em", nullable = false)
    private Instant medidoEm;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;
}

package com.aquamanager.modules.alimentacao.domain;

import com.aquamanager.modules.lote.domain.Lote;
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
@Table(name = "registros_alimentacao")
public class RegistroAlimentacao extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "tipo_racao", nullable = false, length = 100)
    private String tipoRacao;

    @Column(length = 150)
    private String fornecedor;

    @Column(name = "quantidade_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidadeKg;

    @Column(nullable = false)
    private Instant horario;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(precision = 10, scale = 2)
    private BigDecimal custo;
}

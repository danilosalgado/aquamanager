package com.aquamanager.modules.crescimento.domain;

import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "registros_crescimento")
public class RegistroCrescimento extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "peso_medio_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoMedioG;

    @Column(name = "quantidade_amostra", nullable = false)
    private Integer quantidadeAmostra;

    @Column(name = "biomassa_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal biomassaKg;

    @Column(name = "data_pesagem", nullable = false)
    private LocalDate dataPesagem;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;
}

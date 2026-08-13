package com.aquamanager.modules.mortalidade.domain;

import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "registros_mortalidade")
public class RegistroMortalidade extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CausaExclusao causa;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}

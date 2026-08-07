package com.aquamanager.modules.lote.domain;

import com.aquamanager.modules.especie.domain.Especie;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lotes")
public class Lote extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tanque_id", nullable = false)
    private Tanque tanque;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    @Column(length = 150)
    private String fornecedor;

    @Column(name = "quantidade_inicial", nullable = false)
    private Integer quantidadeInicial;

    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual;

    @Column(name = "peso_inicial_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoInicialG;

    @Column(name = "peso_atual_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoAtualG;

    @Column(name = "valor_compra", precision = 12, scale = 2)
    private BigDecimal valorCompra;

    @Column(name = "data_compra", nullable = false)
    private LocalDate dataCompra;

    @Column(name = "previsao_venda")
    private LocalDate previsaoVenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLote status = StatusLote.ATIVO;
}

package com.aquamanager.modules.venda.domain;

import com.aquamanager.modules.cliente.domain.Cliente;
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
@Table(name = "vendas")
public class Venda extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "categoria_produto", nullable = false, length = 80)
    private String categoriaProduto;

    @Column(name = "quantidade_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeKg;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda;

    @Column(length = 500)
    private String observacoes;

    /**
     * Aponta pra receita gerada automaticamente em lancamentos_financeiros quando a
     * venda foi criada — mantém as duas tabelas em sincronia (editar/remover a venda
     * também atualiza/remove essa receita), sem exigir lançamento duplicado.
     */
    @Column(name = "lancamento_financeiro_id")
    private UUID lancamentoFinanceiroId;
}

package com.aquamanager.modules.especie.domain;

import com.aquamanager.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Espécie de piscicultura. Compõe um catálogo global (empresa_id NULL, visível a todos os
 * tenants) mais espécies customizadas por empresa.
 *
 * Estende {@link BaseEntity} diretamente (não {@link com.aquamanager.shared.domain.TenantAwareEntity}):
 * o filtro Hibernate de tenant aplica igualdade estrita (empresa_id = :tenantId), o que excluiria
 * as linhas do catálogo global. O isolamento multi-tenant desta tabela é garantido pela política de
 * Row-Level Security do Postgres (empresa_id IS NULL OR empresa_id = tenant atual), definida em
 * V18__row_level_security.sql. O campo empresaId aqui é apenas informativo/nullable, sem atribuição
 * automática via @PrePersist — a regra de negócio de quem pode criar/editar fica no Service.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "especies")
public class Especie extends BaseEntity {

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "nome_cientifico", length = 120)
    private String nomeCientifico;

    @Column(name = "ciclo_dias_padrao")
    private Integer cicloDiasPadrao;

    @Column(name = "peso_abate_padrao_g", precision = 10, scale = 2)
    private BigDecimal pesoAbatePadraoG;

    @Column(name = "temp_min", precision = 5, scale = 2)
    private BigDecimal tempMin;

    @Column(name = "temp_max", precision = 5, scale = 2)
    private BigDecimal tempMax;

    @Column(name = "ph_min", precision = 4, scale = 2)
    private BigDecimal phMin;

    @Column(name = "ph_max", precision = 4, scale = 2)
    private BigDecimal phMax;

    @Column(name = "oxigenio_min", precision = 5, scale = 2)
    private BigDecimal oxigenioMin;

    @Column(name = "amonia_max", precision = 6, scale = 3)
    private BigDecimal amoniaMax;

    @Column(name = "nitrito_max", precision = 6, scale = 3)
    private BigDecimal nitritoMax;

    @Column(nullable = false)
    private boolean ativo = true;
}

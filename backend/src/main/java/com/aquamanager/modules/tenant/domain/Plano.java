package com.aquamanager.modules.tenant.domain;

import com.aquamanager.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "planos")
public class Plano extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private PlanoCodigo codigo;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(name = "preco_mensal", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoMensal;

    @Column(name = "limite_tanques")
    private Integer limiteTanques;

    @Column(name = "limite_usuarios")
    private Integer limiteUsuarios;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Boolean> recursos = new HashMap<>();

    public boolean possuiRecurso(String chave) {
        return Boolean.TRUE.equals(recursos.get(chave));
    }

    public boolean tanquesIlimitados() {
        return limiteTanques == null;
    }

    public boolean usuariosIlimitados() {
        return limiteUsuarios == null;
    }
}

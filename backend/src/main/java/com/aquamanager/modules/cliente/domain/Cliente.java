package com.aquamanager.modules.cliente.domain;

import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente extends TenantAwareEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String documento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 200)
    private String endereco;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}

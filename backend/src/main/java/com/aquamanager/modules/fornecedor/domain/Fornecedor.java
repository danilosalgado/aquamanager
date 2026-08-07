package com.aquamanager.modules.fornecedor.domain;

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
@Table(name = "fornecedores")
public class Fornecedor extends TenantAwareEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String documento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(name = "produtos_fornecidos", columnDefinition = "TEXT")
    private String produtosFornecidos;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}

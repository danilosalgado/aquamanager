package com.aquamanager.modules.tanque.domain;

import com.aquamanager.shared.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tanque_fotos")
public class TanqueFoto extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tanque_id", nullable = false)
    private Tanque tanque;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private int ordem = 0;
}

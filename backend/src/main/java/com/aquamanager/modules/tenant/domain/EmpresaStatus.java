package com.aquamanager.modules.tenant.domain;

public enum EmpresaStatus {
    TRIAL,
    ATIVA,
    INADIMPLENTE,
    CANCELADA,
    BLOQUEADA;

    /** Empresas nestes status têm o acesso operacional bloqueado (ver PlanGuardFilter). */
    public boolean acessoBloqueado() {
        return this == CANCELADA || this == BLOQUEADA;
    }
}

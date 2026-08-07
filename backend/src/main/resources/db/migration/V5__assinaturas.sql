CREATE TABLE assinaturas (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id              UUID NOT NULL REFERENCES empresas(id),
    plano_id                UUID NOT NULL REFERENCES planos(id),
    status                  VARCHAR(20) NOT NULL
                                CHECK (status IN ('TRIAL', 'ATIVA', 'INADIMPLENTE', 'CANCELADA')),
    asaas_customer_id       VARCHAR(60),
    asaas_subscription_id   VARCHAR(60),
    data_inicio             DATE NOT NULL,
    proximo_vencimento      DATE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_assinaturas_empresa ON assinaturas (empresa_id);

COMMENT ON TABLE assinaturas IS 'Histórico de assinaturas/cobranças por empresa. O estado corrente também é espelhado em empresas.status/plano_id para leitura O(1).';

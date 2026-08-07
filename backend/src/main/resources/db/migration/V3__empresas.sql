CREATE TABLE empresas (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome                VARCHAR(150) NOT NULL,
    documento           VARCHAR(20) NOT NULL UNIQUE, -- CPF ou CNPJ
    endereco            VARCHAR(200),
    cidade              VARCHAR(100),
    estado              CHAR(2),
    telefone            VARCHAR(20),
    email               VARCHAR(150) NOT NULL,
    plano_id            UUID NOT NULL REFERENCES planos(id),
    status              VARCHAR(20) NOT NULL DEFAULT 'TRIAL'
                            CHECK (status IN ('TRIAL', 'ATIVA', 'INADIMPLENTE', 'CANCELADA', 'BLOQUEADA')),
    trial_ends_at       TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE empresas IS 'Tenant raiz da plataforma. status/plano_id são o estado corrente (denormalizado a partir de assinaturas, para leitura rápida em toda requisição).';

CREATE INDEX idx_empresas_status ON empresas (status);

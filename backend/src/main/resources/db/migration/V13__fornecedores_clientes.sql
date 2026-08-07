CREATE TABLE fornecedores (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id              UUID NOT NULL REFERENCES empresas(id),
    nome                    VARCHAR(150) NOT NULL,
    documento               VARCHAR(20),
    telefone                VARCHAR(20),
    email                   VARCHAR(150),
    produtos_fornecidos     TEXT,
    observacoes             TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_fornecedores_empresa ON fornecedores (empresa_id);

CREATE TABLE clientes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID NOT NULL REFERENCES empresas(id),
    nome            VARCHAR(150) NOT NULL,
    documento       VARCHAR(20),
    telefone        VARCHAR(20),
    email           VARCHAR(150),
    endereco        VARCHAR(200),
    observacoes     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_clientes_empresa ON clientes (empresa_id);

CREATE TABLE alertas (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID NOT NULL REFERENCES empresas(id),
    tipo            VARCHAR(60) NOT NULL,
    severidade      VARCHAR(10) NOT NULL CHECK (severidade IN ('INFO', 'ATENCAO', 'CRITICO')),
    titulo          VARCHAR(150) NOT NULL,
    mensagem        VARCHAR(500) NOT NULL,
    entidade_tipo   VARCHAR(60),
    entidade_id     UUID,
    lido            BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_alertas_empresa ON alertas (empresa_id);
CREATE INDEX idx_alertas_lido ON alertas (empresa_id, lido);

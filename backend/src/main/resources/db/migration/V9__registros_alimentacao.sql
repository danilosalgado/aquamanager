CREATE TABLE registros_alimentacao (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID NOT NULL REFERENCES empresas(id),
    lote_id         UUID NOT NULL REFERENCES lotes(id),
    tipo_racao      VARCHAR(100) NOT NULL,
    fornecedor      VARCHAR(150),
    quantidade_kg   NUMERIC(10, 3) NOT NULL,
    horario         TIMESTAMPTZ NOT NULL,
    usuario_id      UUID NOT NULL REFERENCES usuarios(id),
    custo           NUMERIC(10, 2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_registros_alimentacao_empresa ON registros_alimentacao (empresa_id);
CREATE INDEX idx_registros_alimentacao_lote ON registros_alimentacao (lote_id);
CREATE INDEX idx_registros_alimentacao_horario ON registros_alimentacao (horario);

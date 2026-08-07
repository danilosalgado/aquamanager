CREATE TABLE registros_mortalidade (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID NOT NULL REFERENCES empresas(id),
    lote_id         UUID NOT NULL REFERENCES lotes(id),
    quantidade      INTEGER NOT NULL,
    data            DATE NOT NULL,
    motivo          VARCHAR(150) NOT NULL,
    observacoes     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_registros_mortalidade_empresa ON registros_mortalidade (empresa_id);
CREATE INDEX idx_registros_mortalidade_lote ON registros_mortalidade (lote_id);

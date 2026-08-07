CREATE TABLE registros_crescimento (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id          UUID NOT NULL REFERENCES empresas(id),
    lote_id             UUID NOT NULL REFERENCES lotes(id),
    peso_medio_g        NUMERIC(10, 2) NOT NULL,
    quantidade_amostra  INTEGER NOT NULL,
    biomassa_kg         NUMERIC(12, 3) NOT NULL,
    data_pesagem        DATE NOT NULL,
    usuario_id          UUID NOT NULL REFERENCES usuarios(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_registros_crescimento_empresa ON registros_crescimento (empresa_id);
CREATE INDEX idx_registros_crescimento_lote ON registros_crescimento (lote_id);

CREATE TABLE indice_saude_tanque (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID NOT NULL REFERENCES empresas(id),
    tanque_id       UUID NOT NULL REFERENCES tanques(id),
    data            DATE NOT NULL,
    score           INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    classificacao   VARCHAR(10) NOT NULL CHECK (classificacao IN ('EXCELENTE', 'ATENCAO', 'CRITICO')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tanque_id, data)
);

CREATE INDEX idx_indice_saude_empresa ON indice_saude_tanque (empresa_id);
CREATE INDEX idx_indice_saude_tanque ON indice_saude_tanque (tanque_id, data);

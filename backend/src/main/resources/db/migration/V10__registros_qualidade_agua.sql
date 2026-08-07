CREATE TABLE registros_qualidade_agua (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id          UUID NOT NULL REFERENCES empresas(id),
    tanque_id           UUID NOT NULL REFERENCES tanques(id),
    temperatura         NUMERIC(5, 2),
    ph                  NUMERIC(4, 2),
    oxigenio_dissolvido NUMERIC(5, 2),
    amonia              NUMERIC(6, 3),
    nitrito             NUMERIC(6, 3),
    alcalinidade        NUMERIC(6, 2),
    salinidade          NUMERIC(5, 2),
    transparencia_cm    NUMERIC(6, 2),
    medido_em           TIMESTAMPTZ NOT NULL,
    usuario_id          UUID NOT NULL REFERENCES usuarios(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_registros_agua_empresa ON registros_qualidade_agua (empresa_id);
CREATE INDEX idx_registros_agua_tanque ON registros_qualidade_agua (tanque_id);
CREATE INDEX idx_registros_agua_medido_em ON registros_qualidade_agua (medido_em);

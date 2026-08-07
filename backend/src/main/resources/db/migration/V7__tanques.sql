CREATE TABLE tanques (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id              UUID NOT NULL REFERENCES empresas(id),
    nome                    VARCHAR(100) NOT NULL,
    codigo                  VARCHAR(30) NOT NULL,
    tipo                    VARCHAR(20) NOT NULL
                                CHECK (tipo IN ('ESCAVADO', 'REDE', 'CONCRETO', 'BIOFLOCO')),
    area_m2                 NUMERIC(10, 2),
    volume_m3               NUMERIC(10, 2),
    profundidade_m          NUMERIC(6, 2),
    capacidade_estimada_kg  NUMERIC(10, 2),
    latitude                NUMERIC(9, 6),
    longitude               NUMERIC(9, 6),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
                                CHECK (status IN ('ATIVO', 'INATIVO', 'MANUTENCAO')),
    observacoes             TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (empresa_id, codigo)
);

CREATE INDEX idx_tanques_empresa ON tanques (empresa_id);

CREATE TABLE tanque_fotos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id  UUID NOT NULL REFERENCES empresas(id),
    tanque_id   UUID NOT NULL REFERENCES tanques(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    ordem       INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tanque_fotos_tanque ON tanque_fotos (tanque_id);

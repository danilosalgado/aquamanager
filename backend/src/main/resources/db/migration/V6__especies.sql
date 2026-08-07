CREATE TABLE especies (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id              UUID REFERENCES empresas(id), -- NULL = catálogo global (padrão da plataforma)
    nome                    VARCHAR(80) NOT NULL,
    nome_cientifico         VARCHAR(120),
    ciclo_dias_padrao       INTEGER,
    peso_abate_padrao_g     NUMERIC(10, 2),
    -- Faixas ideais usadas pelo motor de alertas e pelo índice de saúde do tanque
    temp_min                NUMERIC(5, 2),
    temp_max                NUMERIC(5, 2),
    ph_min                  NUMERIC(4, 2),
    ph_max                  NUMERIC(4, 2),
    oxigenio_min            NUMERIC(5, 2),
    amonia_max              NUMERIC(6, 3),
    nitrito_max             NUMERIC(6, 3),
    ativo                   BOOLEAN NOT NULL DEFAULT true,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_especies_empresa ON especies (empresa_id);

COMMENT ON TABLE especies IS 'Catálogo global (empresa_id NULL) + espécies customizadas por empresa.';

INSERT INTO especies (nome, nome_cientifico, ciclo_dias_padrao, peso_abate_padrao_g, temp_min, temp_max, ph_min, ph_max, oxigenio_min, amonia_max, nitrito_max) VALUES
    ('Tilápia', 'Oreochromis niloticus', 180, 800, 27, 32, 6.5, 8.5, 4.0, 0.05, 0.1),
    ('Tambaqui', 'Colossoma macropomum', 240, 1200, 26, 30, 6.0, 8.0, 3.5, 0.05, 0.1),
    ('Pacu', 'Piaractus mesopotamicus', 240, 900, 24, 29, 6.0, 8.0, 3.5, 0.05, 0.1),
    ('Pintado', 'Pseudoplatystoma spp.', 300, 1500, 24, 28, 6.5, 8.0, 4.0, 0.03, 0.08),
    ('Tambacu', 'Hybrid Colossoma x Piaractus', 240, 1000, 25, 30, 6.0, 8.0, 3.5, 0.05, 0.1),
    ('Carpa', 'Cyprinus carpio', 210, 1000, 20, 28, 6.5, 9.0, 4.0, 0.05, 0.1);

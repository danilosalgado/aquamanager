CREATE TABLE planos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo              VARCHAR(20) NOT NULL UNIQUE
                            CHECK (codigo IN ('STARTER', 'PROFESSIONAL', 'ENTERPRISE')),
    nome                VARCHAR(60) NOT NULL,
    preco_mensal        NUMERIC(10, 2) NOT NULL,
    limite_tanques      INTEGER, -- NULL = ilimitado
    limite_usuarios     INTEGER, -- NULL = ilimitado
    recursos            JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE planos IS 'Catálogo fixo de planos de assinatura (Starter/Professional/Enterprise).';

INSERT INTO planos (codigo, nome, preco_mensal, limite_tanques, limite_usuarios, recursos) VALUES
    ('STARTER', 'Starter', 149.90, 5, 3,
        '{"dashboard": true, "financeiro": true, "estoque": true, "ia": false, "agenda": false, "relatorios": false, "api": false, "whiteLabel": false, "backupAutomatico": false}'::jsonb),
    ('PROFESSIONAL', 'Professional', 349.90, NULL, NULL,
        '{"dashboard": true, "financeiro": true, "estoque": true, "ia": true, "agenda": true, "relatorios": true, "api": false, "whiteLabel": false, "backupAutomatico": false}'::jsonb),
    ('ENTERPRISE', 'Enterprise', 799.90, NULL, NULL,
        '{"dashboard": true, "financeiro": true, "estoque": true, "ia": true, "agenda": true, "relatorios": true, "api": true, "whiteLabel": true, "backupAutomatico": true}'::jsonb);

CREATE TABLE usuarios (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id              UUID NOT NULL REFERENCES empresas(id),
    nome                    VARCHAR(120) NOT NULL,
    email                   VARCHAR(150) NOT NULL UNIQUE, -- global: login não exige selecionar empresa
    senha_hash              VARCHAR(100) NOT NULL,
    role                    VARCHAR(20) NOT NULL
                                CHECK (role IN ('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO', 'CONSULTOR')),
    ativo                   BOOLEAN NOT NULL DEFAULT true,
    email_confirmado        BOOLEAN NOT NULL DEFAULT false,
    two_factor_enabled      BOOLEAN NOT NULL DEFAULT false,
    two_factor_secret       VARCHAR(64),
    failed_login_attempts   INTEGER NOT NULL DEFAULT 0,
    locked_until            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_usuarios_empresa ON usuarios (empresa_id);

COMMENT ON TABLE usuarios IS 'Tabela de fronteira de autenticação: consultada por e-mail antes de o tenant ser conhecido, portanto NÃO recebe policy de Row-Level Security (ver V18).';

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash      VARCHAR(128) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT false,
    replaced_by_id  UUID REFERENCES refresh_tokens(id),
    ip              VARCHAR(64),
    user_agent      VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_usuario ON refresh_tokens (usuario_id);

CREATE TABLE login_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    empresa_id      UUID REFERENCES empresas(id) ON DELETE SET NULL,
    email_tentativa VARCHAR(150) NOT NULL,
    ip              VARCHAR(64),
    user_agent      VARCHAR(255),
    sucesso         BOOLEAN NOT NULL,
    motivo_falha    VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_login_logs_usuario ON login_logs (usuario_id);
CREATE INDEX idx_login_logs_created_at ON login_logs (created_at);

CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE email_confirmation_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

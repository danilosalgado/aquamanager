CREATE TABLE estoque_itens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id          UUID NOT NULL REFERENCES empresas(id),
    categoria           VARCHAR(20) NOT NULL
                            CHECK (categoria IN ('RACAO', 'MEDICAMENTO', 'QUIMICO', 'EQUIPAMENTO', 'MATERIAL')),
    nome                VARCHAR(150) NOT NULL,
    unidade             VARCHAR(10) NOT NULL,
    quantidade_atual    NUMERIC(12, 3) NOT NULL DEFAULT 0,
    quantidade_minima   NUMERIC(12, 3) NOT NULL DEFAULT 0,
    fornecedor_id       UUID REFERENCES fornecedores(id),
    validade            DATE,
    preco_unitario      NUMERIC(10, 2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_estoque_itens_empresa ON estoque_itens (empresa_id);

CREATE TABLE estoque_movimentacoes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id  UUID NOT NULL REFERENCES empresas(id),
    item_id     UUID NOT NULL REFERENCES estoque_itens(id),
    tipo        VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    quantidade  NUMERIC(12, 3) NOT NULL,
    motivo      VARCHAR(150),
    usuario_id  UUID NOT NULL REFERENCES usuarios(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_estoque_mov_empresa ON estoque_movimentacoes (empresa_id);
CREATE INDEX idx_estoque_mov_item ON estoque_movimentacoes (item_id);

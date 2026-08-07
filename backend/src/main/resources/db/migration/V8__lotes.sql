CREATE TABLE lotes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id          UUID NOT NULL REFERENCES empresas(id),
    tanque_id           UUID NOT NULL REFERENCES tanques(id),
    especie_id          UUID NOT NULL REFERENCES especies(id),
    fornecedor          VARCHAR(150),
    quantidade_inicial  INTEGER NOT NULL,
    quantidade_atual    INTEGER NOT NULL,
    peso_inicial_g      NUMERIC(10, 2) NOT NULL,
    peso_atual_g        NUMERIC(10, 2) NOT NULL,
    valor_compra        NUMERIC(12, 2),
    data_compra         DATE NOT NULL,
    previsao_venda      DATE,
    status              VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
                            CHECK (status IN ('ATIVO', 'VENDIDO', 'ENCERRADO')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lotes_empresa ON lotes (empresa_id);
CREATE INDEX idx_lotes_tanque ON lotes (tanque_id);
CREATE INDEX idx_lotes_status ON lotes (status);

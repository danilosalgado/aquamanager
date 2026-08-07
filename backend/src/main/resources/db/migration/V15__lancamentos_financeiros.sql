CREATE TABLE lancamentos_financeiros (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id          UUID NOT NULL REFERENCES empresas(id),
    tipo                VARCHAR(10) NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    categoria           VARCHAR(60) NOT NULL,
    descricao           VARCHAR(200) NOT NULL,
    valor               NUMERIC(12, 2) NOT NULL,
    data_vencimento     DATE NOT NULL,
    data_pagamento      DATE,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                            CHECK (status IN ('PENDENTE', 'PAGO', 'ATRASADO', 'CANCELADO')),
    forma_pagamento     VARCHAR(30),
    cliente_id          UUID REFERENCES clientes(id),
    fornecedor_id       UUID REFERENCES fornecedores(id),
    lote_id             UUID REFERENCES lotes(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lancamentos_empresa ON lancamentos_financeiros (empresa_id);
CREATE INDEX idx_lancamentos_status ON lancamentos_financeiros (status);
CREATE INDEX idx_lancamentos_vencimento ON lancamentos_financeiros (data_vencimento);

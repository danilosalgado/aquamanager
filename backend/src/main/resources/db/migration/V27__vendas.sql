-- Vendas por categoria de produto (ex.: "Peixe Inteiro Bruto", "Filé"), vinculadas ao
-- lote (e por consequência ao tanque, via lote.tanque_id) — usadas pelo relatório de
-- lucro bruto por tanque. Cada venda gera automaticamente uma receita em
-- lancamentos_financeiros (rastreada via lancamento_financeiro_id) pra não exigir
-- lançamento duplicado e manter o Financeiro/Dashboard batendo com as vendas reais.
CREATE TABLE vendas (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id                UUID NOT NULL REFERENCES empresas(id),
    lote_id                   UUID NOT NULL REFERENCES lotes(id),
    cliente_id                UUID REFERENCES clientes(id),
    categoria_produto         VARCHAR(80) NOT NULL,
    quantidade_kg             NUMERIC(12,3) NOT NULL,
    valor_total               NUMERIC(12,2) NOT NULL,
    data_venda                DATE NOT NULL,
    observacoes               VARCHAR(500),
    lancamento_financeiro_id  UUID REFERENCES lancamentos_financeiros(id),
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vendas_empresa ON vendas (empresa_id);
CREATE INDEX idx_vendas_lote ON vendas (lote_id);

ALTER TABLE vendas ENABLE ROW LEVEL SECURITY;
ALTER TABLE vendas FORCE ROW LEVEL SECURITY;

CREATE POLICY vendas_tenant_isolation ON vendas FOR ALL
    USING (empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true')
    WITH CHECK (empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');

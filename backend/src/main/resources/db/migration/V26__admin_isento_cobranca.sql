-- Contas administrativas da plataforma (ex.: a conta do próprio operador do AquaManager)
-- não devem passar por trial nem conseguir gerar cobrança — elas gerenciam as assinaturas
-- dos clientes, não são clientes.
ALTER TABLE empresas ADD COLUMN isento_cobranca BOOLEAN NOT NULL DEFAULT false;

UPDATE empresas
SET isento_cobranca = true,
    status = 'ATIVA'
WHERE documento = '10200246585' AND email = 'daniloscarvalho66@gmail.com';

UPDATE assinaturas
SET status = 'ATIVA'
WHERE empresa_id IN (
    SELECT id FROM empresas WHERE documento = '10200246585' AND email = 'daniloscarvalho66@gmail.com'
);

-- Consolida o catálogo de planos em um único plano comercial (R$120/mês, todos os
-- recursos habilitados) e generaliza as colunas de identificadores do gateway de
-- pagamento em assinaturas, que até aqui eram nomeadas especificamente para o Asaas
-- mas agora também são usadas pelo StripePaymentGateway.

-- 1) Transforma o plano PROFESSIONAL no plano único da plataforma.
UPDATE planos
SET nome = 'AquaManager',
    preco_mensal = 120.00,
    limite_tanques = NULL,
    limite_usuarios = NULL,
    recursos = '{"dashboard": true, "financeiro": true, "estoque": true, "ia": true, "agenda": true, "relatorios": true, "api": true, "whiteLabel": false, "backupAutomatico": true}'::jsonb
WHERE codigo = 'PROFESSIONAL';

-- 2) Redireciona qualquer empresa/assinatura presa a STARTER ou ENTERPRISE para o
--    plano único antes de remover as linhas antigas (preserva as FKs existentes).
UPDATE empresas
SET plano_id = (SELECT id FROM planos WHERE codigo = 'PROFESSIONAL')
WHERE plano_id IN (SELECT id FROM planos WHERE codigo IN ('STARTER', 'ENTERPRISE'));

UPDATE assinaturas
SET plano_id = (SELECT id FROM planos WHERE codigo = 'PROFESSIONAL')
WHERE plano_id IN (SELECT id FROM planos WHERE codigo IN ('STARTER', 'ENTERPRISE'));

DELETE FROM planos WHERE codigo IN ('STARTER', 'ENTERPRISE');

-- 3) Generaliza os identificadores de gateway em assinaturas (antes específicos do Asaas).
ALTER TABLE assinaturas RENAME COLUMN asaas_customer_id TO gateway_customer_id;
ALTER TABLE assinaturas RENAME COLUMN asaas_subscription_id TO gateway_subscription_id;

COMMENT ON COLUMN assinaturas.gateway_customer_id IS 'ID do cliente no gateway de pagamento ativo (Asaas ou Stripe).';
COMMENT ON COLUMN assinaturas.gateway_subscription_id IS 'ID da assinatura no gateway de pagamento ativo (Asaas ou Stripe).';

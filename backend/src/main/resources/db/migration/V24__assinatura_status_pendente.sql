-- O checkout do Asaas agora cria a assinatura no gateway e redireciona o cliente pra
-- fatura hospedada (PIX/boleto/cartão) ANTES de conceder acesso — a assinatura fica
-- PENDENTE até o webhook confirmar o pagamento. Sem esse status, todo checkout
-- concedia acesso imediato sem cobrança nenhuma ter sido efetivamente paga.

ALTER TABLE assinaturas DROP CONSTRAINT assinaturas_status_check;
ALTER TABLE assinaturas ADD CONSTRAINT assinaturas_status_check
    CHECK (status IN ('PENDENTE', 'TRIAL', 'ATIVA', 'INADIMPLENTE', 'CANCELADA'));

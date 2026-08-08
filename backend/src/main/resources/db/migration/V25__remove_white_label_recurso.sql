-- Remove "whiteLabel" do catálogo de recursos do plano — não é um recurso disponível
-- (nem em roadmap próximo), então não deve nem aparecer riscado na tela de assinatura.
UPDATE planos
SET recursos = recursos - 'whiteLabel';

package com.aquamanager.modules.alerta.domain;

/** Catálogo de tipos de alerta gerados pelo motor de regras (AlertEngineService). */
public enum TipoAlerta {
    QUALIDADE_AGUA_FORA_DA_FAIXA,
    ESTOQUE_BAIXO,
    MORTALIDADE_ELEVADA,
    ALIMENTACAO_INSUFICIENTE,
    DESPESA_ACIMA_RECEITA,
    PAGAMENTO_PROXIMO,
    ASSINATURA_VENCENDO
}

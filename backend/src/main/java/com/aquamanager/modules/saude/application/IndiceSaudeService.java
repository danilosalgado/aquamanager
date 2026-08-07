package com.aquamanager.modules.saude.application;

import com.aquamanager.modules.saude.application.dto.IndiceSaudeHistoricoItem;
import com.aquamanager.modules.saude.application.dto.IndiceSaudeResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IndiceSaudeService {

    /** Calcula o índice de saúde atual do tanque (sob demanda, não necessariamente persistido). */
    IndiceSaudeResponse calcularAtual(UUID empresaId, UUID tanqueId);

    List<IndiceSaudeHistoricoItem> historico(UUID empresaId, UUID tanqueId, LocalDate inicio, LocalDate fim);
}

package com.aquamanager.modules.crescimento.application;

import com.aquamanager.modules.crescimento.application.dto.CrescimentoPotencialResponse;
import java.util.List;
import java.util.UUID;

public interface CrescimentoPotencialService {

    CrescimentoPotencialResponse calcular(UUID empresaId, UUID loteId);

    List<CrescimentoPotencialResponse> calcularTodosAtivos(UUID empresaId);
}

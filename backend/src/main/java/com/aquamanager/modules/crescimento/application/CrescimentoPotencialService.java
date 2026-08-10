package com.aquamanager.modules.crescimento.application;

import com.aquamanager.modules.crescimento.application.dto.CrescimentoPotencialResponse;
import java.util.UUID;

public interface CrescimentoPotencialService {

    CrescimentoPotencialResponse calcular(UUID empresaId, UUID loteId);
}

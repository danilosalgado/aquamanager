package com.aquamanager.modules.alerta.application;

import com.aquamanager.modules.alerta.domain.Alerta;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlertaService {

    Page<Alerta> listar(UUID empresaId, Boolean apenasNaoLidos, Pageable pageable);

    long contarNaoLidos(UUID empresaId);

    Alerta marcarComoLido(UUID empresaId, UUID alertaId);

    void marcarTodosComoLidos(UUID empresaId);
}

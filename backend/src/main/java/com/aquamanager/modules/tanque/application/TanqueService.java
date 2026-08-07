package com.aquamanager.modules.tanque.application;

import com.aquamanager.modules.tanque.application.dto.TanqueRequest;
import com.aquamanager.modules.tanque.domain.Tanque;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TanqueService {

    Page<Tanque> listar(UUID empresaId, Pageable pageable);

    Tanque buscar(UUID empresaId, UUID tanqueId);

    Tanque criar(UUID empresaId, TanqueRequest request);

    Tanque atualizar(UUID empresaId, UUID tanqueId, TanqueRequest request);

    void remover(UUID empresaId, UUID tanqueId);
}

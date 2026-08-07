package com.aquamanager.modules.lote.application;

import com.aquamanager.modules.lote.application.dto.LoteRequest;
import com.aquamanager.modules.lote.domain.Lote;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoteService {

    Page<Lote> listar(UUID empresaId, UUID tanqueId, String status, Pageable pageable);

    Lote buscar(UUID empresaId, UUID loteId);

    Lote criar(UUID empresaId, LoteRequest request);

    Lote atualizar(UUID empresaId, UUID loteId, LoteRequest request);

    void remover(UUID empresaId, UUID loteId);
}

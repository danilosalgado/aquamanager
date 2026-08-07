package com.aquamanager.modules.qualidadeagua.application;

import com.aquamanager.modules.qualidadeagua.application.dto.RegistroQualidadeAguaRequest;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistroQualidadeAguaService {

    Page<RegistroQualidadeAgua> listar(UUID empresaId, UUID tanqueId, Pageable pageable);

    RegistroQualidadeAgua buscar(UUID empresaId, UUID registroId);

    RegistroQualidadeAgua criar(UUID empresaId, UUID usuarioId, RegistroQualidadeAguaRequest request);

    RegistroQualidadeAgua atualizar(UUID empresaId, UUID registroId, RegistroQualidadeAguaRequest request);

    void remover(UUID empresaId, UUID registroId);
}

package com.aquamanager.modules.mortalidade.application;

import com.aquamanager.modules.mortalidade.application.dto.RegistroMortalidadeRequest;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistroMortalidadeService {

    Page<RegistroMortalidade> listar(UUID empresaId, UUID loteId, Pageable pageable);

    RegistroMortalidade buscar(UUID empresaId, UUID registroId);

    RegistroMortalidade criar(UUID empresaId, RegistroMortalidadeRequest request);

    RegistroMortalidade atualizar(UUID empresaId, UUID registroId, RegistroMortalidadeRequest request);

    void remover(UUID empresaId, UUID registroId);
}

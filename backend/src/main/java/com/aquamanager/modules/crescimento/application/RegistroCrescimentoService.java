package com.aquamanager.modules.crescimento.application;

import com.aquamanager.modules.crescimento.application.dto.RegistroCrescimentoRequest;
import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistroCrescimentoService {

    Page<RegistroCrescimento> listar(UUID empresaId, UUID loteId, Pageable pageable);

    RegistroCrescimento buscar(UUID empresaId, UUID registroId);

    RegistroCrescimento criar(UUID empresaId, UUID usuarioId, RegistroCrescimentoRequest request);

    RegistroCrescimento atualizar(UUID empresaId, UUID registroId, RegistroCrescimentoRequest request);

    void remover(UUID empresaId, UUID registroId);
}

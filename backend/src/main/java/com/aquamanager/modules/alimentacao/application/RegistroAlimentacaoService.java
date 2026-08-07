package com.aquamanager.modules.alimentacao.application;

import com.aquamanager.modules.alimentacao.application.dto.RegistroAlimentacaoRequest;
import com.aquamanager.modules.alimentacao.domain.RegistroAlimentacao;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistroAlimentacaoService {

    Page<RegistroAlimentacao> listar(UUID empresaId, UUID loteId, Pageable pageable);

    RegistroAlimentacao buscar(UUID empresaId, UUID registroId);

    RegistroAlimentacao criar(UUID empresaId, UUID usuarioId, RegistroAlimentacaoRequest request);

    RegistroAlimentacao atualizar(UUID empresaId, UUID registroId, RegistroAlimentacaoRequest request);

    void remover(UUID empresaId, UUID registroId);
}

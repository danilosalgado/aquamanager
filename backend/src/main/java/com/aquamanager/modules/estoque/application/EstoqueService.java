package com.aquamanager.modules.estoque.application;

import com.aquamanager.modules.estoque.application.dto.EstoqueItemRequest;
import com.aquamanager.modules.estoque.application.dto.MovimentacaoRequest;
import com.aquamanager.modules.estoque.domain.EstoqueItem;
import com.aquamanager.modules.estoque.domain.EstoqueMovimentacao;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EstoqueService {

    Page<EstoqueItem> listarItens(UUID empresaId, Pageable pageable);

    EstoqueItem buscarItem(UUID empresaId, UUID itemId);

    EstoqueItem criarItem(UUID empresaId, EstoqueItemRequest request);

    EstoqueItem atualizarItem(UUID empresaId, UUID itemId, EstoqueItemRequest request);

    void removerItem(UUID empresaId, UUID itemId);

    EstoqueMovimentacao registrarMovimentacao(UUID empresaId, MovimentacaoRequest request);

    Page<EstoqueMovimentacao> listarMovimentacoes(UUID empresaId, UUID itemId, Pageable pageable);
}

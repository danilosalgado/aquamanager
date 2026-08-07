package com.aquamanager.modules.estoque.infrastructure.mapper;

import com.aquamanager.modules.estoque.application.dto.MovimentacaoResponse;
import com.aquamanager.modules.estoque.domain.EstoqueMovimentacao;
import org.springframework.stereotype.Component;

/** Mapper manual (não MapStruct): resolve o nome do item associado. */
@Component
public class EstoqueMovimentacaoMapper {

    public MovimentacaoResponse toResponse(EstoqueMovimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getItem().getId(),
                movimentacao.getItem().getNome(),
                movimentacao.getTipo().name(),
                movimentacao.getQuantidade(),
                movimentacao.getMotivo(),
                movimentacao.getUsuarioId(),
                movimentacao.getCreatedAt()
        );
    }
}

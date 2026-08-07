package com.aquamanager.modules.estoque.infrastructure.mapper;

import com.aquamanager.modules.estoque.application.dto.EstoqueItemResponse;
import com.aquamanager.modules.estoque.domain.EstoqueItem;
import org.springframework.stereotype.Component;

/** Mapper manual (não MapStruct): resolve o nome do fornecedor associado. */
@Component
public class EstoqueItemMapper {

    public EstoqueItemResponse toResponse(EstoqueItem item) {
        return new EstoqueItemResponse(
                item.getId(),
                item.getCategoria().name(),
                item.getNome(),
                item.getUnidade(),
                item.getQuantidadeAtual(),
                item.getQuantidadeMinima(),
                item.getFornecedor() != null ? item.getFornecedor().getId() : null,
                item.getFornecedor() != null ? item.getFornecedor().getNome() : null,
                item.getValidade(),
                item.getPrecoUnitario()
        );
    }
}

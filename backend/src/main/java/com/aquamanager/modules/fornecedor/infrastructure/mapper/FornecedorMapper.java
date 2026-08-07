package com.aquamanager.modules.fornecedor.infrastructure.mapper;

import com.aquamanager.modules.fornecedor.application.dto.FornecedorResponse;
import com.aquamanager.modules.fornecedor.domain.Fornecedor;
import org.springframework.stereotype.Component;

/** Mapper manual (não MapStruct), seguindo o padrão dos demais módulos. */
@Component
public class FornecedorMapper {

    public FornecedorResponse toResponse(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getDocumento(),
                fornecedor.getTelefone(),
                fornecedor.getEmail(),
                fornecedor.getProdutosFornecidos(),
                fornecedor.getObservacoes()
        );
    }
}

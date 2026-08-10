package com.aquamanager.modules.venda.infrastructure.mapper;

import com.aquamanager.modules.venda.application.dto.VendaResponse;
import com.aquamanager.modules.venda.domain.Venda;
import org.springframework.stereotype.Component;

@Component
public class VendaMapper {

    public VendaResponse toResponse(Venda v) {
        return new VendaResponse(
                v.getId(),
                v.getLote().getId(),
                v.getLote().getTanque().getNome(),
                v.getLote().getEspecie().getNome(),
                v.getCliente() != null ? v.getCliente().getId() : null,
                v.getCliente() != null ? v.getCliente().getNome() : null,
                v.getCategoriaProduto(),
                v.getQuantidadeKg(),
                v.getValorTotal(),
                v.getDataVenda(),
                v.getObservacoes()
        );
    }
}

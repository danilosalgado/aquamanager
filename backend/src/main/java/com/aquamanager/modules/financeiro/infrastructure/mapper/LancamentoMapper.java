package com.aquamanager.modules.financeiro.infrastructure.mapper;

import com.aquamanager.modules.financeiro.application.dto.LancamentoResponse;
import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import org.springframework.stereotype.Component;

@Component
public class LancamentoMapper {

    public LancamentoResponse toResponse(LancamentoFinanceiro l) {
        return new LancamentoResponse(
                l.getId(),
                l.getTipo().name(),
                l.getCategoria(),
                l.getDescricao(),
                l.getValor(),
                l.getDataVencimento(),
                l.getDataPagamento(),
                l.getStatus().name(),
                l.getFormaPagamento(),
                l.getCliente() != null ? l.getCliente().getId() : null,
                l.getCliente() != null ? l.getCliente().getNome() : null,
                l.getFornecedor() != null ? l.getFornecedor().getId() : null,
                l.getFornecedor() != null ? l.getFornecedor().getNome() : null,
                l.getLote() != null ? l.getLote().getId() : null
        );
    }
}

package com.aquamanager.modules.lote.infrastructure.mapper;

import com.aquamanager.modules.lote.application.dto.LoteResponse;
import com.aquamanager.modules.lote.domain.Lote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** Mapper manual (não MapStruct): cálculo de biomassa atual e achatamento de tanque/espécie não valem a geração automática. */
@Component
public class LoteMapper {

    private static final BigDecimal MIL = BigDecimal.valueOf(1000);

    public LoteResponse toResponse(Lote lote) {
        BigDecimal biomassaAtualKg = BigDecimal.valueOf(lote.getQuantidadeAtual())
                .multiply(lote.getPesoAtualG())
                .divide(MIL, 3, RoundingMode.HALF_UP);

        return new LoteResponse(
                lote.getId(),
                lote.getTanque().getId(),
                lote.getTanque().getNome(),
                lote.getEspecie().getId(),
                lote.getEspecie().getNome(),
                lote.getFornecedor(),
                lote.getQuantidadeInicial(),
                lote.getQuantidadeAtual(),
                lote.getPesoInicialG(),
                lote.getPesoAtualG(),
                lote.getValorCompra(),
                lote.getDataCompra(),
                lote.getPrevisaoVenda(),
                lote.getStatus().name(),
                biomassaAtualKg
        );
    }
}

package com.aquamanager.modules.especie.infrastructure.mapper;

import com.aquamanager.modules.especie.application.dto.EspecieResponse;
import com.aquamanager.modules.especie.domain.Especie;
import org.springframework.stereotype.Component;

/** Mapper manual (não MapStruct): cálculo do flag "global" a partir de empresaId não vale a geração automática. */
@Component
public class EspecieMapper {

    public EspecieResponse toResponse(Especie especie) {
        return new EspecieResponse(
                especie.getId(),
                especie.getNome(),
                especie.getNomeCientifico(),
                especie.getCicloDiasPadrao(),
                especie.getPesoAbatePadraoG(),
                especie.getTempMin(),
                especie.getTempMax(),
                especie.getPhMin(),
                especie.getPhMax(),
                especie.getOxigenioMin(),
                especie.getAmoniaMax(),
                especie.getNitritoMax(),
                especie.isAtivo(),
                especie.getEmpresaId() == null
        );
    }
}

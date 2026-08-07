package com.aquamanager.modules.tanque.infrastructure.mapper;

import com.aquamanager.modules.tanque.application.dto.TanqueResponse;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.domain.TanqueFoto;
import org.springframework.stereotype.Component;

/** Mapper manual (não MapStruct): conversões de enum e relação fotos->urls não valem a geração automática. */
@Component
public class TanqueMapper {

    public TanqueResponse toResponse(Tanque tanque) {
        return new TanqueResponse(
                tanque.getId(),
                tanque.getNome(),
                tanque.getCodigo(),
                tanque.getTipo().name(),
                tanque.getAreaM2(),
                tanque.getVolumeM3(),
                tanque.getProfundidadeM(),
                tanque.getCapacidadeEstimadaKg(),
                tanque.getLatitude(),
                tanque.getLongitude(),
                tanque.getStatus().name(),
                tanque.getObservacoes(),
                tanque.getFotos().stream().map(TanqueFoto::getUrl).toList()
        );
    }
}

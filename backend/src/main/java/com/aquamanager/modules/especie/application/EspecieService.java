package com.aquamanager.modules.especie.application;

import com.aquamanager.modules.especie.application.dto.EspecieRequest;
import com.aquamanager.modules.especie.domain.Especie;
import java.util.List;
import java.util.UUID;

public interface EspecieService {

    List<Especie> listar(UUID empresaId);

    Especie criar(UUID empresaId, EspecieRequest request);

    Especie atualizar(UUID empresaId, UUID especieId, EspecieRequest request);

    void remover(UUID empresaId, UUID especieId);
}

package com.aquamanager.modules.especie.application;

import com.aquamanager.modules.especie.application.dto.EspecieRequest;
import com.aquamanager.modules.especie.domain.Especie;
import com.aquamanager.modules.especie.infrastructure.persistence.EspecieRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EspecieServiceImpl implements EspecieService {

    private final EspecieRepository especieRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Especie> listar(UUID empresaId) {
        return especieRepository.findByEmpresaIdIsNullOrEmpresaIdOrderByNomeAsc(empresaId);
    }

    @Override
    @Transactional
    public Especie criar(UUID empresaId, EspecieRequest request) {
        // Toda criação via API pertence ao tenant atual; nunca se cria uma linha do catálogo global por aqui.
        Especie especie = new Especie();
        especie.setEmpresaId(empresaId);
        aplicarCampos(especie, request);
        return especieRepository.save(especie);
    }

    @Override
    @Transactional
    public Especie atualizar(UUID empresaId, UUID especieId, EspecieRequest request) {
        Especie especie = buscarEditavel(empresaId, especieId);
        aplicarCampos(especie, request);
        return especie;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID especieId) {
        Especie especie = buscarEditavel(empresaId, especieId);
        especieRepository.delete(especie);
    }

    private Especie buscarEditavel(UUID empresaId, UUID especieId) {
        Especie especie = especieRepository.findById(especieId)
                .orElseThrow(() -> new ResourceNotFoundException("Espécie", especieId));
        if (especie.getEmpresaId() == null || !especie.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("READONLY_GLOBAL_SPECIES",
                    "Não é possível editar uma espécie do catálogo global.");
        }
        return especie;
    }

    private void aplicarCampos(Especie especie, EspecieRequest request) {
        especie.setNome(request.nome());
        especie.setNomeCientifico(request.nomeCientifico());
        especie.setCicloDiasPadrao(request.cicloDiasPadrao());
        especie.setPesoAbatePadraoG(request.pesoAbatePadraoG());
        especie.setTempMin(request.tempMin());
        especie.setTempMax(request.tempMax());
        especie.setPhMin(request.phMin());
        especie.setPhMax(request.phMax());
        especie.setOxigenioMin(request.oxigenioMin());
        especie.setAmoniaMax(request.amoniaMax());
        especie.setNitritoMax(request.nitritoMax());
        especie.setAtivo(request.ativo() == null || request.ativo());
    }
}

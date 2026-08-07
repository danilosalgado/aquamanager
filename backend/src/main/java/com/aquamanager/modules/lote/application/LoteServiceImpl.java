package com.aquamanager.modules.lote.application;

import com.aquamanager.modules.especie.domain.Especie;
import com.aquamanager.modules.especie.infrastructure.persistence.EspecieRepository;
import com.aquamanager.modules.lote.application.dto.LoteRequest;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoteServiceImpl implements LoteService {

    private final LoteRepository loteRepository;
    private final TanqueRepository tanqueRepository;
    private final EspecieRepository especieRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Lote> listar(UUID empresaId, UUID tanqueId, String status, Pageable pageable) {
        StatusLote statusLote = status != null ? parseStatus(status) : null;

        if (tanqueId != null && statusLote != null) {
            return loteRepository.findByEmpresaIdAndTanqueIdAndStatus(empresaId, tanqueId, statusLote, pageable);
        }
        if (tanqueId != null) {
            return loteRepository.findByEmpresaIdAndTanqueId(empresaId, tanqueId, pageable);
        }
        if (statusLote != null) {
            return loteRepository.findByEmpresaIdAndStatus(empresaId, statusLote, pageable);
        }
        return loteRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Lote buscar(UUID empresaId, UUID loteId) {
        return buscarDaEmpresa(empresaId, loteId);
    }

    @Override
    @Transactional
    public Lote criar(UUID empresaId, LoteRequest request) {
        Tanque tanque = tanqueRepository.findById(request.tanqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Tanque", request.tanqueId()));
        if (!tanque.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Tanque", request.tanqueId());
        }

        Especie especie = especieRepository.findById(request.especieId())
                .orElseThrow(() -> new ResourceNotFoundException("Espécie", request.especieId()));
        if (especie.getEmpresaId() != null && !especie.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Espécie", request.especieId());
        }

        Lote lote = new Lote();
        lote.setEmpresaId(empresaId);
        lote.setTanque(tanque);
        lote.setEspecie(especie);
        lote.setFornecedor(request.fornecedor());
        lote.setQuantidadeInicial(request.quantidadeInicial());
        lote.setQuantidadeAtual(request.quantidadeInicial());
        lote.setPesoInicialG(request.pesoInicialG());
        lote.setPesoAtualG(request.pesoInicialG());
        lote.setValorCompra(request.valorCompra());
        lote.setDataCompra(request.dataCompra());
        lote.setPrevisaoVenda(request.previsaoVenda());
        lote.setStatus(request.status() != null ? parseStatus(request.status()) : StatusLote.ATIVO);

        return loteRepository.save(lote);
    }

    @Override
    @Transactional
    public Lote atualizar(UUID empresaId, UUID loteId, LoteRequest request) {
        Lote lote = buscarDaEmpresa(empresaId, loteId);

        // tanque, espécie, quantidadeInicial e pesoInicialG não são editáveis após a criação do lote;
        // campos do request referentes a eles são ignorados silenciosamente.
        lote.setFornecedor(request.fornecedor());
        lote.setValorCompra(request.valorCompra());
        lote.setPrevisaoVenda(request.previsaoVenda());
        if (request.status() != null) {
            lote.setStatus(parseStatus(request.status()));
        }

        return lote;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID loteId) {
        Lote lote = buscarDaEmpresa(empresaId, loteId);
        loteRepository.delete(lote);
    }

    private Lote buscarDaEmpresa(UUID empresaId, UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));
        if (!lote.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Lote", loteId);
        }
        return lote;
    }

    private StatusLote parseStatus(String value) {
        try {
            return StatusLote.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ENUM_VALUE", "Valor inválido para status do lote: " + value);
        }
    }
}

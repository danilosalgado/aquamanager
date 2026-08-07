package com.aquamanager.modules.alimentacao.application;

import com.aquamanager.modules.alimentacao.application.dto.RegistroAlimentacaoRequest;
import com.aquamanager.modules.alimentacao.domain.RegistroAlimentacao;
import com.aquamanager.modules.alimentacao.infrastructure.persistence.RegistroAlimentacaoRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistroAlimentacaoServiceImpl implements RegistroAlimentacaoService {

    private final RegistroAlimentacaoRepository registroAlimentacaoRepository;
    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAlimentacao> listar(UUID empresaId, UUID loteId, Pageable pageable) {
        if (loteId != null) {
            return registroAlimentacaoRepository.findByEmpresaIdAndLoteId(empresaId, loteId, pageable);
        }
        return registroAlimentacaoRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroAlimentacao buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroAlimentacao criar(UUID empresaId, UUID usuarioId, RegistroAlimentacaoRequest request) {
        Lote lote = buscarLoteDaEmpresa(empresaId, request.loteId());

        RegistroAlimentacao registro = new RegistroAlimentacao();
        registro.setEmpresaId(empresaId);
        registro.setLote(lote);
        registro.setUsuarioId(usuarioId);
        aplicarCampos(registro, request);
        return registroAlimentacaoRepository.save(registro);
    }

    @Override
    @Transactional
    public RegistroAlimentacao atualizar(UUID empresaId, UUID registroId, RegistroAlimentacaoRequest request) {
        RegistroAlimentacao registro = buscarDaEmpresa(empresaId, registroId);
        if (!registro.getLote().getId().equals(request.loteId())) {
            registro.setLote(buscarLoteDaEmpresa(empresaId, request.loteId()));
        }
        aplicarCampos(registro, request);
        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroAlimentacao registro = buscarDaEmpresa(empresaId, registroId);
        registroAlimentacaoRepository.delete(registro);
    }

    private RegistroAlimentacao buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroAlimentacao registro = registroAlimentacaoRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de alimentação", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de alimentação", registroId);
        }
        return registro;
    }

    private Lote buscarLoteDaEmpresa(UUID empresaId, UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));
        if (!lote.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Lote", loteId);
        }
        return lote;
    }

    private void aplicarCampos(RegistroAlimentacao registro, RegistroAlimentacaoRequest request) {
        registro.setTipoRacao(request.tipoRacao());
        registro.setFornecedor(request.fornecedor());
        registro.setQuantidadeKg(request.quantidadeKg());
        registro.setHorario(request.horario());
        registro.setCusto(request.custo());
    }
}

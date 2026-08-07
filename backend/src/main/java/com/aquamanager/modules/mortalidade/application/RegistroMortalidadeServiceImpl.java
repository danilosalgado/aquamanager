package com.aquamanager.modules.mortalidade.application;

import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.mortalidade.application.dto.RegistroMortalidadeRequest;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import com.aquamanager.modules.mortalidade.infrastructure.persistence.RegistroMortalidadeRepository;
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
public class RegistroMortalidadeServiceImpl implements RegistroMortalidadeService {

    private final RegistroMortalidadeRepository registroMortalidadeRepository;
    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroMortalidade> listar(UUID empresaId, UUID loteId, Pageable pageable) {
        if (loteId != null) {
            return registroMortalidadeRepository.findByEmpresaIdAndLoteId(empresaId, loteId, pageable);
        }
        return registroMortalidadeRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroMortalidade buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroMortalidade criar(UUID empresaId, RegistroMortalidadeRequest request) {
        Lote lote = buscarLoteDaEmpresa(empresaId, request.loteId());

        RegistroMortalidade registro = new RegistroMortalidade();
        registro.setEmpresaId(empresaId);
        registro.setLote(lote);
        aplicarCampos(registro, request);

        decrementarEstoque(lote, request.quantidade());
        loteRepository.save(lote);

        return registroMortalidadeRepository.save(registro);
    }

    @Override
    @Transactional
    public RegistroMortalidade atualizar(UUID empresaId, UUID registroId, RegistroMortalidadeRequest request) {
        RegistroMortalidade registro = buscarDaEmpresa(empresaId, registroId);
        Lote lote = registro.getLote().getId().equals(request.loteId())
                ? registro.getLote()
                : buscarLoteDaEmpresa(empresaId, request.loteId());

        // Estorna a quantidade anteriormente descontada do lote original antes de aplicar o novo valor.
        Lote loteOriginal = registro.getLote();
        loteOriginal.setQuantidadeAtual(loteOriginal.getQuantidadeAtual() + registro.getQuantidade());
        if (!loteOriginal.getId().equals(lote.getId())) {
            loteRepository.save(loteOriginal);
        }

        decrementarEstoque(lote, request.quantidade());
        loteRepository.save(lote);

        registro.setLote(lote);
        aplicarCampos(registro, request);
        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroMortalidade registro = buscarDaEmpresa(empresaId, registroId);
        Lote lote = registro.getLote();
        lote.setQuantidadeAtual(lote.getQuantidadeAtual() + registro.getQuantidade());
        loteRepository.save(lote);
        registroMortalidadeRepository.delete(registro);
    }

    private void decrementarEstoque(Lote lote, Integer quantidade) {
        if (quantidade > lote.getQuantidadeAtual()) {
            throw new BusinessException("MORTALIDADE_INVALIDA",
                    "Quantidade de mortalidade maior que o estoque atual do lote.");
        }
        lote.setQuantidadeAtual(lote.getQuantidadeAtual() - quantidade);
    }

    private RegistroMortalidade buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroMortalidade registro = registroMortalidadeRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de mortalidade", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de mortalidade", registroId);
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

    private void aplicarCampos(RegistroMortalidade registro, RegistroMortalidadeRequest request) {
        registro.setQuantidade(request.quantidade());
        registro.setData(request.data());
        registro.setMotivo(request.motivo());
        registro.setObservacoes(request.observacoes());
    }
}

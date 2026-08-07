package com.aquamanager.modules.crescimento.application;

import com.aquamanager.modules.crescimento.application.dto.RegistroCrescimentoRequest;
import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import com.aquamanager.modules.crescimento.infrastructure.persistence.RegistroCrescimentoRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistroCrescimentoServiceImpl implements RegistroCrescimentoService {

    private static final int ESCALA_BIOMASSA = 3;

    private final RegistroCrescimentoRepository registroCrescimentoRepository;
    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroCrescimento> listar(UUID empresaId, UUID loteId, Pageable pageable) {
        if (loteId != null) {
            return registroCrescimentoRepository.findByEmpresaIdAndLoteId(empresaId, loteId, pageable);
        }
        return registroCrescimentoRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroCrescimento buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroCrescimento criar(UUID empresaId, UUID usuarioId, RegistroCrescimentoRequest request) {
        Lote lote = buscarLoteDaEmpresa(empresaId, request.loteId());

        RegistroCrescimento registro = new RegistroCrescimento();
        registro.setEmpresaId(empresaId);
        registro.setLote(lote);
        registro.setUsuarioId(usuarioId);
        registro.setPesoMedioG(request.pesoMedioG());
        registro.setQuantidadeAmostra(request.quantidadeAmostra());
        registro.setDataPesagem(request.dataPesagem());
        registro.setBiomassaKg(calcularBiomassaKg(lote, request.pesoMedioG()));

        RegistroCrescimento salvo = registroCrescimentoRepository.save(registro);

        lote.setPesoAtualG(request.pesoMedioG());
        loteRepository.save(lote);

        return salvo;
    }

    @Override
    @Transactional
    public RegistroCrescimento atualizar(UUID empresaId, UUID registroId, RegistroCrescimentoRequest request) {
        RegistroCrescimento registro = buscarDaEmpresa(empresaId, registroId);
        Lote lote = registro.getLote().getId().equals(request.loteId())
                ? registro.getLote()
                : buscarLoteDaEmpresa(empresaId, request.loteId());

        registro.setLote(lote);
        registro.setPesoMedioG(request.pesoMedioG());
        registro.setQuantidadeAmostra(request.quantidadeAmostra());
        registro.setDataPesagem(request.dataPesagem());
        registro.setBiomassaKg(calcularBiomassaKg(lote, request.pesoMedioG()));

        lote.setPesoAtualG(request.pesoMedioG());
        loteRepository.save(lote);

        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroCrescimento registro = buscarDaEmpresa(empresaId, registroId);
        registroCrescimentoRepository.delete(registro);
    }

    private BigDecimal calcularBiomassaKg(Lote lote, BigDecimal pesoMedioG) {
        BigDecimal quantidadeAtual = BigDecimal.valueOf(lote.getQuantidadeAtual());
        return quantidadeAtual.multiply(pesoMedioG)
                .divide(BigDecimal.valueOf(1000), ESCALA_BIOMASSA, RoundingMode.HALF_UP);
    }

    private RegistroCrescimento buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroCrescimento registro = registroCrescimentoRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de crescimento", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de crescimento", registroId);
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
}

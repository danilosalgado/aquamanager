package com.aquamanager.modules.qualidadeagua.application;

import com.aquamanager.modules.qualidadeagua.application.dto.RegistroQualidadeAguaRequest;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import com.aquamanager.modules.qualidadeagua.infrastructure.persistence.RegistroQualidadeAguaRepository;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistroQualidadeAguaServiceImpl implements RegistroQualidadeAguaService {

    private final RegistroQualidadeAguaRepository registroQualidadeAguaRepository;
    private final TanqueRepository tanqueRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroQualidadeAgua> listar(UUID empresaId, UUID tanqueId, Pageable pageable) {
        if (tanqueId != null) {
            return registroQualidadeAguaRepository.findByEmpresaIdAndTanqueId(empresaId, tanqueId, pageable);
        }
        return registroQualidadeAguaRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroQualidadeAgua buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroQualidadeAgua criar(UUID empresaId, UUID usuarioId, RegistroQualidadeAguaRequest request) {
        Tanque tanque = buscarTanqueDaEmpresa(empresaId, request.tanqueId());

        RegistroQualidadeAgua registro = new RegistroQualidadeAgua();
        registro.setEmpresaId(empresaId);
        registro.setTanque(tanque);
        registro.setUsuarioId(usuarioId);
        aplicarCampos(registro, request);
        return registroQualidadeAguaRepository.save(registro);
    }

    @Override
    @Transactional
    public RegistroQualidadeAgua atualizar(UUID empresaId, UUID registroId, RegistroQualidadeAguaRequest request) {
        RegistroQualidadeAgua registro = buscarDaEmpresa(empresaId, registroId);
        if (!registro.getTanque().getId().equals(request.tanqueId())) {
            registro.setTanque(buscarTanqueDaEmpresa(empresaId, request.tanqueId()));
        }
        aplicarCampos(registro, request);
        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroQualidadeAgua registro = buscarDaEmpresa(empresaId, registroId);
        registroQualidadeAguaRepository.delete(registro);
    }

    private RegistroQualidadeAgua buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroQualidadeAgua registro = registroQualidadeAguaRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de qualidade de água", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de qualidade de água", registroId);
        }
        return registro;
    }

    private Tanque buscarTanqueDaEmpresa(UUID empresaId, UUID tanqueId) {
        Tanque tanque = tanqueRepository.findById(tanqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Tanque", tanqueId));
        if (!tanque.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Tanque", tanqueId);
        }
        return tanque;
    }

    private void aplicarCampos(RegistroQualidadeAgua registro, RegistroQualidadeAguaRequest request) {
        registro.setTemperatura(request.temperatura());
        registro.setPh(request.ph());
        registro.setOxigenioDissolvido(request.oxigenioDissolvido());
        registro.setAmonia(request.amonia());
        registro.setNitrito(request.nitrito());
        registro.setAlcalinidade(request.alcalinidade());
        registro.setSalinidade(request.salinidade());
        registro.setTransparenciaCm(request.transparenciaCm());
        registro.setMedidoEm(request.medidoEm());
    }
}

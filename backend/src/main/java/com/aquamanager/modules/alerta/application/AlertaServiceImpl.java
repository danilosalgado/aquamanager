package com.aquamanager.modules.alerta.application;

import com.aquamanager.modules.alerta.domain.Alerta;
import com.aquamanager.modules.alerta.infrastructure.persistence.AlertaRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaRepository alertaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Alerta> listar(UUID empresaId, Boolean apenasNaoLidos, Pageable pageable) {
        if (Boolean.TRUE.equals(apenasNaoLidos)) {
            return alertaRepository.findByEmpresaIdAndLidoOrderByCreatedAtDesc(empresaId, false, pageable);
        }
        return alertaRepository.findByEmpresaIdOrderByCreatedAtDesc(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNaoLidos(UUID empresaId) {
        return alertaRepository.countByEmpresaIdAndLido(empresaId, false);
    }

    @Override
    @Transactional
    public Alerta marcarComoLido(UUID empresaId, UUID alertaId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", alertaId));
        if (!alerta.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Alerta", alertaId);
        }
        alerta.setLido(true);
        return alerta;
    }

    @Override
    @Transactional
    public void marcarTodosComoLidos(UUID empresaId) {
        alertaRepository.marcarTodosComoLidos(empresaId);
    }
}

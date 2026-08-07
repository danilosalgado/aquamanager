package com.aquamanager.modules.tanque.application;

import com.aquamanager.modules.tanque.application.dto.TanqueRequest;
import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.domain.TanqueFoto;
import com.aquamanager.modules.tanque.domain.TipoTanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.modules.tenant.application.EmpresaService;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ConflictException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TanqueServiceImpl implements TanqueService {

    private final TanqueRepository tanqueRepository;
    private final EmpresaService empresaService;

    @Override
    @Transactional(readOnly = true)
    public Page<Tanque> listar(UUID empresaId, Pageable pageable) {
        return tanqueRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Tanque buscar(UUID empresaId, UUID tanqueId) {
        return buscarDaEmpresa(empresaId, tanqueId);
    }

    @Override
    @Transactional
    public Tanque criar(UUID empresaId, TanqueRequest request) {
        empresaService.garantirLimiteTanques(empresaId, tanqueRepository.countByEmpresaId(empresaId));

        if (tanqueRepository.existsByEmpresaIdAndCodigoIgnoreCase(empresaId, request.codigo())) {
            throw new ConflictException("Já existe um tanque com o código " + request.codigo());
        }

        Tanque tanque = new Tanque();
        tanque.setEmpresaId(empresaId);
        aplicarCampos(tanque, request);
        return tanqueRepository.save(tanque);
    }

    @Override
    @Transactional
    public Tanque atualizar(UUID empresaId, UUID tanqueId, TanqueRequest request) {
        Tanque tanque = buscarDaEmpresa(empresaId, tanqueId);
        aplicarCampos(tanque, request);
        return tanque;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID tanqueId) {
        Tanque tanque = buscarDaEmpresa(empresaId, tanqueId);
        tanqueRepository.delete(tanque);
    }

    private Tanque buscarDaEmpresa(UUID empresaId, UUID tanqueId) {
        Tanque tanque = tanqueRepository.findById(tanqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Tanque", tanqueId));
        if (!tanque.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Tanque", tanqueId);
        }
        return tanque;
    }

    private void aplicarCampos(Tanque tanque, TanqueRequest request) {
        tanque.setNome(request.nome());
        tanque.setCodigo(request.codigo());
        tanque.setTipo(parseEnum(TipoTanque.class, request.tipo(), "tipo de tanque"));
        tanque.setAreaM2(request.areaM2());
        tanque.setVolumeM3(request.volumeM3());
        tanque.setProfundidadeM(request.profundidadeM());
        tanque.setCapacidadeEstimadaKg(request.capacidadeEstimadaKg());
        tanque.setLatitude(request.latitude());
        tanque.setLongitude(request.longitude());
        tanque.setStatus(request.status() != null
                ? parseEnum(StatusTanque.class, request.status(), "status do tanque")
                : StatusTanque.ATIVO);
        tanque.setObservacoes(request.observacoes());

        tanque.getFotos().clear();
        if (request.fotos() != null) {
            var novasFotos = new ArrayList<TanqueFoto>();
            int ordem = 0;
            for (String url : request.fotos()) {
                TanqueFoto foto = new TanqueFoto();
                foto.setEmpresaId(tanque.getEmpresaId());
                foto.setTanque(tanque);
                foto.setUrl(url);
                foto.setOrdem(ordem++);
                novasFotos.add(foto);
            }
            tanque.getFotos().addAll(novasFotos);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String descricao) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ENUM_VALUE", "Valor inválido para " + descricao + ": " + value);
        }
    }
}

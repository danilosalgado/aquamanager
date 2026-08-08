package com.aquamanager.modules.admin.application;

import com.aquamanager.modules.admin.application.dto.UsuarioAdminResponse;
import com.aquamanager.modules.auth.application.dto.AtualizarUsuarioRequest;
import com.aquamanager.modules.auth.domain.Usuario;
import com.aquamanager.modules.auth.infrastructure.persistence.UsuarioRepository;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.shared.domain.Role;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ForbiddenException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import com.aquamanager.shared.infrastructure.persistence.TenantSessionManager;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUsuarioServiceImpl implements AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final TenantSessionManager tenantSessionManager;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioAdminResponse> listarTodos(UUID chamadorEmpresaId, Pageable pageable) {
        exigirContaAdministrativa(chamadorEmpresaId);
        return tenantSessionManager.runAsSystem(() -> {
            Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
            var empresaIds = usuarios.getContent().stream().map(Usuario::getEmpresaId).distinct().toList();
            Map<UUID, String> nomesPorEmpresa = empresaRepository.findAllById(empresaIds).stream()
                    .collect(Collectors.toMap(Empresa::getId, Empresa::getNome));
            return usuarios.map(u -> toResponse(u, nomesPorEmpresa.get(u.getEmpresaId())));
        });
    }

    @Override
    @Transactional
    public UsuarioAdminResponse atualizar(UUID chamadorEmpresaId, UUID usuarioId, AtualizarUsuarioRequest request) {
        exigirContaAdministrativa(chamadorEmpresaId);
        return tenantSessionManager.runAsSystem(() -> {
            Usuario usuario = buscar(usuarioId);
            usuario.setNome(request.nome());
            usuario.setRole(parseRole(request.role()));
            usuario.setAtivo(request.ativo());
            String nomeEmpresa = empresaRepository.findById(usuario.getEmpresaId())
                    .map(Empresa::getNome).orElse(null);
            return toResponse(usuario, nomeEmpresa);
        });
    }

    @Override
    @Transactional
    public void desativar(UUID chamadorEmpresaId, UUID usuarioId) {
        exigirContaAdministrativa(chamadorEmpresaId);
        tenantSessionManager.runAsSystem(() -> {
            buscar(usuarioId).setAtivo(false);
        });
    }

    private Usuario buscar(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
    }

    /**
     * Único portão de entrada pro bypass de tenant deste serviço: sem essa checagem
     * passando, nenhum método aqui chega perto de {@code runAsSystem}.
     */
    private void exigirContaAdministrativa(UUID empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
        if (!empresa.isIsentoCobranca()) {
            throw new ForbiddenException("Acesso restrito a contas administrativas da plataforma.");
        }
    }

    private static UsuarioAdminResponse toResponse(Usuario usuario, String empresaNome) {
        return new UsuarioAdminResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name(),
                usuario.isAtivo(),
                usuario.getEmpresaId(),
                empresaNome != null ? empresaNome : "—"
        );
    }

    private static Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ROLE", "Papel inválido: " + role);
        }
    }
}

package com.aquamanager.modules.auth.application;

import com.aquamanager.modules.auth.application.dto.AtualizarUsuarioRequest;
import com.aquamanager.modules.auth.application.dto.CriarUsuarioRequest;
import com.aquamanager.modules.auth.domain.Usuario;
import com.aquamanager.modules.auth.infrastructure.persistence.UsuarioRepository;
import com.aquamanager.modules.tenant.application.EmpresaService;
import com.aquamanager.shared.domain.Role;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaService empresaService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listar(UUID empresaId, Pageable pageable) {
        return usuarioRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional
    public Usuario criar(UUID empresaId, CriarUsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Já existe uma conta com este e-mail.");
        }
        empresaService.garantirLimiteUsuarios(empresaId, usuarioRepository.countByEmpresaId(empresaId));

        Usuario usuario = new Usuario();
        usuario.setEmpresaId(empresaId);
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRole(parseRole(request.role()));
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario atualizar(UUID empresaId, UUID usuarioId, AtualizarUsuarioRequest request) {
        Usuario usuario = buscarDaEmpresa(empresaId, usuarioId);
        usuario.setNome(request.nome());
        usuario.setRole(parseRole(request.role()));
        usuario.setAtivo(request.ativo());
        return usuario;
    }

    @Override
    @Transactional
    public void desativar(UUID empresaId, UUID usuarioId) {
        Usuario usuario = buscarDaEmpresa(empresaId, usuarioId);
        usuario.setAtivo(false);
    }

    private Usuario buscarDaEmpresa(UUID empresaId, UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
        if (!usuario.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Usuário", usuarioId);
        }
        return usuario;
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ROLE", "Papel inválido: " + role);
        }
    }
}

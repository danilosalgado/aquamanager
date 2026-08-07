package com.aquamanager.modules.auth.application;

import com.aquamanager.modules.auth.application.dto.AtualizarUsuarioRequest;
import com.aquamanager.modules.auth.application.dto.CriarUsuarioRequest;
import com.aquamanager.modules.auth.domain.Usuario;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    Page<Usuario> listar(UUID empresaId, Pageable pageable);

    Usuario criar(UUID empresaId, CriarUsuarioRequest request);

    Usuario atualizar(UUID empresaId, UUID usuarioId, AtualizarUsuarioRequest request);

    void desativar(UUID empresaId, UUID usuarioId);
}

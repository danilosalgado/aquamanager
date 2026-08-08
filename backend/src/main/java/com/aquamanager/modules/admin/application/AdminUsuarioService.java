package com.aquamanager.modules.admin.application;

import com.aquamanager.modules.admin.application.dto.UsuarioAdminResponse;
import com.aquamanager.modules.auth.application.dto.AtualizarUsuarioRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Gestão de usuários entre todos os tenants, restrita a contas administrativas da
 * plataforma (empresa com {@code isentoCobranca=true}) — quem chama nunca é o dono
 * dos dados que está lendo/alterando, então cada método reverifica essa condição.
 */
public interface AdminUsuarioService {

    Page<UsuarioAdminResponse> listarTodos(UUID chamadorEmpresaId, Pageable pageable);

    UsuarioAdminResponse atualizar(UUID chamadorEmpresaId, UUID usuarioId, AtualizarUsuarioRequest request);

    void desativar(UUID chamadorEmpresaId, UUID usuarioId);
}

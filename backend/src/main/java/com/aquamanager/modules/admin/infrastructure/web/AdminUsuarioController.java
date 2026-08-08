package com.aquamanager.modules.admin.infrastructure.web;

import com.aquamanager.modules.admin.application.AdminUsuarioService;
import com.aquamanager.modules.admin.application.dto.UsuarioAdminResponse;
import com.aquamanager.modules.auth.application.dto.AtualizarUsuarioRequest;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import com.aquamanager.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestão de usuários entre todos os tenants — só acessível a contas administrativas
 * da plataforma. O {@code @PreAuthorize} aqui é só a primeira camada (usuário
 * precisa ser ADMINISTRADOR da própria empresa); a checagem que realmente importa
 * ({@code isentoCobranca}) acontece dentro de {@code AdminUsuarioServiceImpl}.
 */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    @GetMapping
    public ApiResponse<PageResponse<UsuarioAdminResponse>> listarTodos(Pageable pageable) {
        var page = adminUsuarioService.listarTodos(SecurityUtils.currentEmpresaId(), pageable);
        return ApiResponse.of(PageResponse.from(page));
    }

    @PutMapping("/{id}")
    public ApiResponse<UsuarioAdminResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarUsuarioRequest request) {
        var usuario = adminUsuarioService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(usuario);
    }

    @DeleteMapping("/{id}")
    public void desativar(@PathVariable UUID id) {
        adminUsuarioService.desativar(SecurityUtils.currentEmpresaId(), id);
    }
}

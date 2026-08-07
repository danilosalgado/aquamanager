package com.aquamanager.modules.auth.infrastructure.web;

import com.aquamanager.modules.auth.application.UsuarioService;
import com.aquamanager.modules.auth.application.dto.AtualizarUsuarioRequest;
import com.aquamanager.modules.auth.application.dto.CriarUsuarioRequest;
import com.aquamanager.modules.auth.application.dto.UsuarioResponse;
import com.aquamanager.modules.auth.infrastructure.mapper.UsuarioMapper;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping
    public ApiResponse<PageResponse<UsuarioResponse>> listar(Pageable pageable) {
        var page = usuarioService.listar(SecurityUtils.currentEmpresaId(), pageable).map(usuarioMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        var usuario = usuarioService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(usuarioMapper.toResponse(usuario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<UsuarioResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarUsuarioRequest request) {
        var usuario = usuarioService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(usuarioMapper.toResponse(usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void desativar(@PathVariable UUID id) {
        usuarioService.desativar(SecurityUtils.currentEmpresaId(), id);
    }
}

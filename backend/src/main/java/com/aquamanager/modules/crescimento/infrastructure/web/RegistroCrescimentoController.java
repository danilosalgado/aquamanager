package com.aquamanager.modules.crescimento.infrastructure.web;

import com.aquamanager.modules.crescimento.application.RegistroCrescimentoService;
import com.aquamanager.modules.crescimento.application.dto.RegistroCrescimentoRequest;
import com.aquamanager.modules.crescimento.application.dto.RegistroCrescimentoResponse;
import com.aquamanager.modules.crescimento.infrastructure.mapper.RegistroCrescimentoMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crescimento")
@RequiredArgsConstructor
public class RegistroCrescimentoController {

    private final RegistroCrescimentoService registroCrescimentoService;
    private final RegistroCrescimentoMapper registroCrescimentoMapper;

    @GetMapping
    public ApiResponse<PageResponse<RegistroCrescimentoResponse>> listar(
            @RequestParam(required = false) UUID loteId, Pageable pageable) {
        var page = registroCrescimentoService.listar(SecurityUtils.currentEmpresaId(), loteId, pageable)
                .map(registroCrescimentoMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<RegistroCrescimentoResponse> buscar(@PathVariable UUID id) {
        var registro = registroCrescimentoService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(registroCrescimentoMapper.toResponse(registro));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroCrescimentoResponse> criar(@Valid @RequestBody RegistroCrescimentoRequest request) {
        var registro = registroCrescimentoService.criar(
                SecurityUtils.currentEmpresaId(), SecurityUtils.currentUserId(), request);
        return ApiResponse.of(registroCrescimentoMapper.toResponse(registro));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroCrescimentoResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody RegistroCrescimentoRequest request) {
        var registro = registroCrescimentoService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(registroCrescimentoMapper.toResponse(registro));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public void remover(@PathVariable UUID id) {
        registroCrescimentoService.remover(SecurityUtils.currentEmpresaId(), id);
    }
}

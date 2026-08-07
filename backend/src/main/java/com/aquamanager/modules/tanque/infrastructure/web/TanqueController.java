package com.aquamanager.modules.tanque.infrastructure.web;

import com.aquamanager.modules.tanque.application.TanqueService;
import com.aquamanager.modules.tanque.application.dto.TanqueRequest;
import com.aquamanager.modules.tanque.application.dto.TanqueResponse;
import com.aquamanager.modules.tanque.infrastructure.mapper.TanqueMapper;
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
@RequestMapping("/api/v1/tanques")
@RequiredArgsConstructor
public class TanqueController {

    private final TanqueService tanqueService;
    private final TanqueMapper tanqueMapper;

    @GetMapping
    public ApiResponse<PageResponse<TanqueResponse>> listar(Pageable pageable) {
        var page = tanqueService.listar(SecurityUtils.currentEmpresaId(), pageable).map(tanqueMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<TanqueResponse> buscar(@PathVariable UUID id) {
        var tanque = tanqueService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(tanqueMapper.toResponse(tanque));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<TanqueResponse> criar(@Valid @RequestBody TanqueRequest request) {
        var tanque = tanqueService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(tanqueMapper.toResponse(tanque));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<TanqueResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody TanqueRequest request) {
        var tanque = tanqueService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(tanqueMapper.toResponse(tanque));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void remover(@PathVariable UUID id) {
        tanqueService.remover(SecurityUtils.currentEmpresaId(), id);
    }
}

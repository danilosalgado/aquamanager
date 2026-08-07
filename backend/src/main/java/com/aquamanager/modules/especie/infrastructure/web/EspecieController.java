package com.aquamanager.modules.especie.infrastructure.web;

import com.aquamanager.modules.especie.application.EspecieService;
import com.aquamanager.modules.especie.application.dto.EspecieRequest;
import com.aquamanager.modules.especie.application.dto.EspecieResponse;
import com.aquamanager.modules.especie.infrastructure.mapper.EspecieMapper;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/especies")
@RequiredArgsConstructor
public class EspecieController {

    private final EspecieService especieService;
    private final EspecieMapper especieMapper;

    @GetMapping
    public ApiResponse<List<EspecieResponse>> listar() {
        var especies = especieService.listar(SecurityUtils.currentEmpresaId()).stream()
                .map(especieMapper::toResponse)
                .toList();
        return ApiResponse.of(especies);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<EspecieResponse> criar(@Valid @RequestBody EspecieRequest request) {
        var especie = especieService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(especieMapper.toResponse(especie));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<EspecieResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody EspecieRequest request) {
        var especie = especieService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(especieMapper.toResponse(especie));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void remover(@PathVariable UUID id) {
        especieService.remover(SecurityUtils.currentEmpresaId(), id);
    }
}

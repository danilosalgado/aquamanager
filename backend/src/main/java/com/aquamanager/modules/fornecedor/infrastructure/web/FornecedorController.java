package com.aquamanager.modules.fornecedor.infrastructure.web;

import com.aquamanager.modules.fornecedor.application.FornecedorService;
import com.aquamanager.modules.fornecedor.application.dto.FornecedorRequest;
import com.aquamanager.modules.fornecedor.application.dto.FornecedorResponse;
import com.aquamanager.modules.fornecedor.infrastructure.mapper.FornecedorMapper;
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
@RequestMapping("/api/v1/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;
    private final FornecedorMapper fornecedorMapper;

    @GetMapping
    public ApiResponse<PageResponse<FornecedorResponse>> listar(
            @RequestParam(required = false) String busca, Pageable pageable) {
        var page = fornecedorService.listar(SecurityUtils.currentEmpresaId(), busca, pageable)
                .map(fornecedorMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<FornecedorResponse> buscar(@PathVariable UUID id) {
        var fornecedor = fornecedorService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(fornecedorMapper.toResponse(fornecedor));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        var fornecedor = fornecedorService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(fornecedorMapper.toResponse(fornecedor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<FornecedorResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody FornecedorRequest request) {
        var fornecedor = fornecedorService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(fornecedorMapper.toResponse(fornecedor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void remover(@PathVariable UUID id) {
        fornecedorService.remover(SecurityUtils.currentEmpresaId(), id);
    }
}

package com.aquamanager.modules.tenant.infrastructure.web;

import com.aquamanager.modules.tenant.application.EmpresaService;
import com.aquamanager.modules.tenant.application.dto.AtualizarEmpresaRequest;
import com.aquamanager.modules.tenant.application.dto.EmpresaResponse;
import com.aquamanager.modules.tenant.infrastructure.mapper.EmpresaMapper;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaMapper empresaMapper;

    @GetMapping("/me")
    public ApiResponse<EmpresaResponse> minhaEmpresa() {
        var empresa = empresaService.buscarPorId(SecurityUtils.currentEmpresaId());
        return ApiResponse.of(empresaMapper.toResponse(empresa));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<EmpresaResponse> atualizarMinhaEmpresa(@Valid @RequestBody AtualizarEmpresaRequest request) {
        var empresa = empresaService.atualizar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(empresaMapper.toResponse(empresa));
    }
}

package com.aquamanager.modules.cliente.infrastructure.web;

import com.aquamanager.modules.cliente.application.ClienteService;
import com.aquamanager.modules.cliente.application.dto.ClienteRequest;
import com.aquamanager.modules.cliente.application.dto.ClienteResponse;
import com.aquamanager.modules.cliente.infrastructure.mapper.ClienteMapper;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @GetMapping
    public ApiResponse<PageResponse<ClienteResponse>> listar(
            @RequestParam(required = false) String busca, Pageable pageable) {
        var page = clienteService.listar(SecurityUtils.currentEmpresaId(), busca, pageable)
                .map(clienteMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClienteResponse> buscar(@PathVariable UUID id) {
        var cliente = clienteService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(clienteMapper.toResponse(cliente));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        var cliente = clienteService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(clienteMapper.toResponse(cliente));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<ClienteResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody ClienteRequest request) {
        var cliente = clienteService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(clienteMapper.toResponse(cliente));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void remover(@PathVariable UUID id) {
        clienteService.remover(SecurityUtils.currentEmpresaId(), id);
    }
}

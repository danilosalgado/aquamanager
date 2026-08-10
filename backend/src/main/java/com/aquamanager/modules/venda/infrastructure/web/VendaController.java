package com.aquamanager.modules.venda.infrastructure.web;

import com.aquamanager.modules.venda.application.VendaService;
import com.aquamanager.modules.venda.application.dto.ResumoLucroBrutoResponse;
import com.aquamanager.modules.venda.application.dto.VendaRequest;
import com.aquamanager.modules.venda.application.dto.VendaResponse;
import com.aquamanager.modules.venda.infrastructure.mapper.VendaMapper;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import com.aquamanager.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;
    private final VendaMapper vendaMapper;

    @GetMapping
    public ApiResponse<PageResponse<VendaResponse>> listar(
            @RequestParam(required = false) String categoriaProduto,
            Pageable pageable) {
        var page = vendaService.listar(SecurityUtils.currentEmpresaId(), categoriaProduto, pageable)
                .map(vendaMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<VendaResponse> buscar(@PathVariable UUID id) {
        var venda = vendaService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(vendaMapper.toResponse(venda));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<VendaResponse> criar(@Valid @RequestBody VendaRequest request) {
        var venda = vendaService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(vendaMapper.toResponse(venda));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<VendaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody VendaRequest request) {
        var venda = vendaService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(vendaMapper.toResponse(venda));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void remover(@PathVariable UUID id) {
        vendaService.remover(SecurityUtils.currentEmpresaId(), id);
    }

    @GetMapping("/categorias")
    public ApiResponse<List<String>> listarCategorias() {
        return ApiResponse.of(vendaService.listarCategorias(SecurityUtils.currentEmpresaId()));
    }

    @GetMapping("/relatorio/lucro-bruto")
    public ApiResponse<ResumoLucroBrutoResponse> resumoLucroBruto() {
        return ApiResponse.of(vendaService.resumoLucroBruto(SecurityUtils.currentEmpresaId()));
    }
}

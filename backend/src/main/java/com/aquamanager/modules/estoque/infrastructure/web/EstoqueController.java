package com.aquamanager.modules.estoque.infrastructure.web;

import com.aquamanager.modules.estoque.application.EstoqueService;
import com.aquamanager.modules.estoque.application.dto.EstoqueItemRequest;
import com.aquamanager.modules.estoque.application.dto.EstoqueItemResponse;
import com.aquamanager.modules.estoque.application.dto.MovimentacaoRequest;
import com.aquamanager.modules.estoque.application.dto.MovimentacaoResponse;
import com.aquamanager.modules.estoque.infrastructure.mapper.EstoqueItemMapper;
import com.aquamanager.modules.estoque.infrastructure.mapper.EstoqueMovimentacaoMapper;
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
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;
    private final EstoqueItemMapper estoqueItemMapper;
    private final EstoqueMovimentacaoMapper estoqueMovimentacaoMapper;

    @GetMapping("/itens")
    public ApiResponse<PageResponse<EstoqueItemResponse>> listarItens(Pageable pageable) {
        var page = estoqueService.listarItens(SecurityUtils.currentEmpresaId(), pageable)
                .map(estoqueItemMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/itens/{id}")
    public ApiResponse<EstoqueItemResponse> buscarItem(@PathVariable UUID id) {
        var item = estoqueService.buscarItem(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(estoqueItemMapper.toResponse(item));
    }

    @PostMapping("/itens")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<EstoqueItemResponse> criarItem(@Valid @RequestBody EstoqueItemRequest request) {
        var item = estoqueService.criarItem(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(estoqueItemMapper.toResponse(item));
    }

    @PutMapping("/itens/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<EstoqueItemResponse> atualizarItem(
            @PathVariable UUID id, @Valid @RequestBody EstoqueItemRequest request) {
        var item = estoqueService.atualizarItem(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(estoqueItemMapper.toResponse(item));
    }

    @DeleteMapping("/itens/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void removerItem(@PathVariable UUID id) {
        estoqueService.removerItem(SecurityUtils.currentEmpresaId(), id);
    }

    @GetMapping("/movimentacoes")
    public ApiResponse<PageResponse<MovimentacaoResponse>> listarMovimentacoes(
            @RequestParam(required = false) UUID itemId, Pageable pageable) {
        var page = estoqueService.listarMovimentacoes(SecurityUtils.currentEmpresaId(), itemId, pageable)
                .map(estoqueMovimentacaoMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @PostMapping("/movimentacoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<MovimentacaoResponse> registrarMovimentacao(@Valid @RequestBody MovimentacaoRequest request) {
        var movimentacao = estoqueService.registrarMovimentacao(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(estoqueMovimentacaoMapper.toResponse(movimentacao));
    }
}

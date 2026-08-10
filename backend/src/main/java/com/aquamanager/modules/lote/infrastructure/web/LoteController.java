package com.aquamanager.modules.lote.infrastructure.web;

import com.aquamanager.modules.crescimento.application.CrescimentoPotencialService;
import com.aquamanager.modules.crescimento.application.dto.CrescimentoPotencialResponse;
import com.aquamanager.modules.lote.application.LoteService;
import com.aquamanager.modules.lote.application.dto.LoteRequest;
import com.aquamanager.modules.lote.application.dto.LoteResponse;
import com.aquamanager.modules.lote.infrastructure.mapper.LoteMapper;
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
@RequestMapping("/api/v1/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;
    private final LoteMapper loteMapper;
    private final CrescimentoPotencialService crescimentoPotencialService;

    @GetMapping
    public ApiResponse<PageResponse<LoteResponse>> listar(
            @RequestParam(required = false) UUID tanqueId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        var page = loteService.listar(SecurityUtils.currentEmpresaId(), tanqueId, status, pageable)
                .map(loteMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<LoteResponse> buscar(@PathVariable UUID id) {
        var lote = loteService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(loteMapper.toResponse(lote));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<LoteResponse> criar(@Valid @RequestBody LoteRequest request) {
        var lote = loteService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(loteMapper.toResponse(lote));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<LoteResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody LoteRequest request) {
        var lote = loteService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(loteMapper.toResponse(lote));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void remover(@PathVariable UUID id) {
        loteService.remover(SecurityUtils.currentEmpresaId(), id);
    }

    @GetMapping("/{id}/crescimento-potencial")
    public ApiResponse<CrescimentoPotencialResponse> crescimentoPotencial(@PathVariable UUID id) {
        return ApiResponse.of(crescimentoPotencialService.calcular(SecurityUtils.currentEmpresaId(), id));
    }

    @GetMapping("/crescimento-potencial")
    public ApiResponse<List<CrescimentoPotencialResponse>> crescimentoPotencialTodosAtivos() {
        return ApiResponse.of(crescimentoPotencialService.calcularTodosAtivos(SecurityUtils.currentEmpresaId()));
    }
}

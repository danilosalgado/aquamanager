package com.aquamanager.modules.agenda.infrastructure.web;

import com.aquamanager.modules.agenda.application.EventoService;
import com.aquamanager.modules.agenda.application.dto.EventoRequest;
import com.aquamanager.modules.agenda.application.dto.EventoResponse;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import com.aquamanager.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<EventoResponse> criar(@Valid @RequestBody EventoRequest request) {
        return ApiResponse.of(eventoService.criar(SecurityUtils.currentEmpresaId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<EventoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EventoRequest request) {
        return ApiResponse.of(eventoService.atualizar(SecurityUtils.currentEmpresaId(), id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public void remover(@PathVariable UUID id) {
        eventoService.remover(SecurityUtils.currentEmpresaId(), id);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventoResponse> buscarPorId(@PathVariable UUID id) {
        return ApiResponse.of(eventoService.buscarPorId(SecurityUtils.currentEmpresaId(), id));
    }

    @GetMapping
    public ApiResponse<PageResponse<EventoResponse>> listar(Pageable pageable) {
        return ApiResponse.of(PageResponse.from(eventoService.listar(SecurityUtils.currentEmpresaId(), pageable)));
    }

    @GetMapping("/periodo")
    public ApiResponse<List<EventoResponse>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return ApiResponse.of(eventoService.listarPorPeriodo(SecurityUtils.currentEmpresaId(), inicio, fim));
    }

    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public void alternarConcluido(@PathVariable UUID id) {
        eventoService.alternarConcluido(SecurityUtils.currentEmpresaId(), id);
    }
}

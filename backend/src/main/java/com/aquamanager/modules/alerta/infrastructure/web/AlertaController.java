package com.aquamanager.modules.alerta.infrastructure.web;

import com.aquamanager.modules.alerta.application.AlertaService;
import com.aquamanager.modules.alerta.application.dto.AlertaResponse;
import com.aquamanager.modules.alerta.infrastructure.mapper.AlertaMapper;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import com.aquamanager.shared.infrastructure.web.PageResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;
    private final AlertaMapper alertaMapper;

    @GetMapping
    public ApiResponse<PageResponse<AlertaResponse>> listar(
            @RequestParam(required = false) Boolean naoLidos, Pageable pageable) {
        var page = alertaService.listar(SecurityUtils.currentEmpresaId(), naoLidos, pageable).map(alertaMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/contagem-nao-lidos")
    public ApiResponse<Map<String, Long>> contarNaoLidos() {
        return ApiResponse.of(Map.of("total", alertaService.contarNaoLidos(SecurityUtils.currentEmpresaId())));
    }

    @PostMapping("/{id}/marcar-lido")
    public ApiResponse<AlertaResponse> marcarComoLido(@PathVariable UUID id) {
        var alerta = alertaService.marcarComoLido(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(alertaMapper.toResponse(alerta));
    }

    @PostMapping("/marcar-todos-lidos")
    public void marcarTodosComoLidos() {
        alertaService.marcarTodosComoLidos(SecurityUtils.currentEmpresaId());
    }
}

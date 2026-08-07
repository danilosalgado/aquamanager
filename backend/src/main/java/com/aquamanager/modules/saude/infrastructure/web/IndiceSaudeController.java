package com.aquamanager.modules.saude.infrastructure.web;

import com.aquamanager.modules.saude.application.IndiceSaudeService;
import com.aquamanager.modules.saude.application.dto.IndiceSaudeHistoricoItem;
import com.aquamanager.modules.saude.application.dto.IndiceSaudeResponse;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tanques/{tanqueId}/indice-saude")
@RequiredArgsConstructor
public class IndiceSaudeController {

    private final IndiceSaudeService indiceSaudeService;

    @GetMapping
    public ApiResponse<IndiceSaudeResponse> atual(@PathVariable UUID tanqueId) {
        return ApiResponse.of(indiceSaudeService.calcularAtual(SecurityUtils.currentEmpresaId(), tanqueId));
    }

    @GetMapping("/historico")
    public ApiResponse<List<IndiceSaudeHistoricoItem>> historico(
            @PathVariable UUID tanqueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ApiResponse.of(indiceSaudeService.historico(SecurityUtils.currentEmpresaId(), tanqueId, inicio, fim));
    }
}

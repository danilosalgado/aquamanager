package com.aquamanager.modules.dashboard.infrastructure.web;

import com.aquamanager.modules.dashboard.application.DashboardService;
import com.aquamanager.modules.dashboard.application.dto.DashboardResumoResponse;
import com.aquamanager.modules.dashboard.application.dto.ProducaoPorTanqueResponse;
import com.aquamanager.modules.dashboard.application.dto.QualidadeAguaPoint;
import com.aquamanager.modules.dashboard.application.dto.SaudeMediaDiariaResponse;
import com.aquamanager.modules.dashboard.application.dto.SeriePontoResponse;
import com.aquamanager.modules.dashboard.application.dto.TimeSeriesPoint;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    public ApiResponse<DashboardResumoResponse> resumo() {
        return ApiResponse.of(dashboardService.resumo(SecurityUtils.currentEmpresaId()));
    }

    @GetMapping("/grafico-financeiro")
    public ApiResponse<List<SeriePontoResponse>> graficoFinanceiro(@RequestParam(defaultValue = "6") int meses) {
        return ApiResponse.of(dashboardService.graficoFinanceiro(SecurityUtils.currentEmpresaId(), meses));
    }

    @GetMapping("/grafico-indice-saude")
    public ApiResponse<List<SaudeMediaDiariaResponse>> graficoIndiceSaude(@RequestParam(defaultValue = "30") int dias) {
        return ApiResponse.of(dashboardService.graficoIndiceSaudeMedio(SecurityUtils.currentEmpresaId(), dias));
    }

    @GetMapping("/grafico-crescimento")
    public ApiResponse<List<TimeSeriesPoint>> graficoCrescimento(@RequestParam(defaultValue = "30") int dias) {
        return ApiResponse.of(dashboardService.graficoCrescimento(SecurityUtils.currentEmpresaId(), dias));
    }

    @GetMapping("/grafico-consumo-racao")
    public ApiResponse<List<TimeSeriesPoint>> graficoConsumoRacao(@RequestParam(defaultValue = "30") int dias) {
        return ApiResponse.of(dashboardService.graficoConsumoRacao(SecurityUtils.currentEmpresaId(), dias));
    }

    @GetMapping("/grafico-mortalidade")
    public ApiResponse<List<TimeSeriesPoint>> graficoMortalidade(@RequestParam(defaultValue = "30") int dias) {
        return ApiResponse.of(dashboardService.graficoMortalidade(SecurityUtils.currentEmpresaId(), dias));
    }

    @GetMapping("/grafico-qualidade-agua")
    public ApiResponse<List<QualidadeAguaPoint>> graficoQualidadeAgua(@RequestParam(defaultValue = "30") int dias) {
        return ApiResponse.of(dashboardService.graficoQualidadeAgua(SecurityUtils.currentEmpresaId(), dias));
    }

    @GetMapping("/grafico-biomassa")
    public ApiResponse<List<TimeSeriesPoint>> graficoBiomassa(@RequestParam(defaultValue = "90") int dias) {
        return ApiResponse.of(dashboardService.graficoBiomassa(SecurityUtils.currentEmpresaId(), dias));
    }

    @GetMapping("/producao-por-tanque")
    public ApiResponse<List<ProducaoPorTanqueResponse>> producaoPorTanque() {
        return ApiResponse.of(dashboardService.producaoPorTanque(SecurityUtils.currentEmpresaId()));
    }
}


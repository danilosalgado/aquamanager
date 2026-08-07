package com.aquamanager.modules.dashboard.application;

import com.aquamanager.modules.dashboard.application.dto.DashboardResumoResponse;
import com.aquamanager.modules.dashboard.application.dto.ProducaoPorTanqueResponse;
import com.aquamanager.modules.dashboard.application.dto.QualidadeAguaPoint;
import com.aquamanager.modules.dashboard.application.dto.SaudeMediaDiariaResponse;
import com.aquamanager.modules.dashboard.application.dto.SeriePontoResponse;
import com.aquamanager.modules.dashboard.application.dto.TimeSeriesPoint;
import java.util.List;
import java.util.UUID;

public interface DashboardService {

    DashboardResumoResponse resumo(UUID empresaId);

    List<SeriePontoResponse> graficoFinanceiro(UUID empresaId, int meses);

    List<SaudeMediaDiariaResponse> graficoIndiceSaudeMedio(UUID empresaId, int dias);

    /** Peso médio (g) ao longo do tempo. */
    List<TimeSeriesPoint> graficoCrescimento(UUID empresaId, int dias);

    /** Consumo de ração (kg/dia) ao longo do tempo. */
    List<TimeSeriesPoint> graficoConsumoRacao(UUID empresaId, int dias);

    /** Mortalidade (count/dia) ao longo do tempo. */
    List<TimeSeriesPoint> graficoMortalidade(UUID empresaId, int dias);

    /** Temperatura, pH, O₂ médios por dia. */
    List<QualidadeAguaPoint> graficoQualidadeAgua(UUID empresaId, int dias);

    /** Biomassa total (kg) ao longo do tempo. */
    List<TimeSeriesPoint> graficoBiomassa(UUID empresaId, int dias);

    /** Dados de produção por tanque ativo. */
    List<ProducaoPorTanqueResponse> producaoPorTanque(UUID empresaId);
}

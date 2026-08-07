import { apiClient } from '@/lib/api-client'
import type { ApiResponse } from '@/types/api'

export interface DashboardResumo {
  quantidadeTanques: number
  quantidadeLotesAtivos: number
  quantidadePeixes: number
  biomassaTotalKg: number
  pesoMedioG: number
  conversaoAlimentarMedia: number
  receita30Dias: number
  despesa30Dias: number
  lucro30Dias: number
  mortalidade30Dias: number
  alertasNaoLidos: number
  indiceSaudeMedio: number | null
}

export interface SeriePonto {
  rotulo: string
  valorReceita: number
  valorDespesa: number
}

export interface SaudeMediaDiaria {
  data: string
  scoreMedio: number
}

export interface TimeSeriesPoint {
  data: string
  valor: number
}

export interface QualidadeAguaPoint {
  data: string
  temperatura: number | null
  ph: number | null
  oxigenioDissolvido: number | null
}

export interface ProducaoPorTanqueResponse {
  tanqueNome: string
  biomassaKg: number
  quantidadePeixes: number
  indiceSaude: number | null
}

export const dashboardApi = {
  resumo: () => apiClient.get<ApiResponse<DashboardResumo>>('/dashboard/resumo').then((r) => r.data.data),

  graficoFinanceiro: (meses = 6) =>
    apiClient
      .get<ApiResponse<SeriePonto[]>>('/dashboard/grafico-financeiro', { params: { meses } })
      .then((r) => r.data.data),

  graficoIndiceSaude: (dias = 30) =>
    apiClient
      .get<ApiResponse<SaudeMediaDiaria[]>>('/dashboard/grafico-indice-saude', { params: { dias } })
      .then((r) => r.data.data),

  graficoCrescimento: (dias = 30) =>
    apiClient
      .get<ApiResponse<TimeSeriesPoint[]>>('/dashboard/grafico-crescimento', { params: { dias } })
      .then((r) => r.data.data),

  graficoConsumoRacao: (dias = 30) =>
    apiClient
      .get<ApiResponse<TimeSeriesPoint[]>>('/dashboard/grafico-consumo-racao', { params: { dias } })
      .then((r) => r.data.data),

  graficoMortalidade: (dias = 30) =>
    apiClient
      .get<ApiResponse<TimeSeriesPoint[]>>('/dashboard/grafico-mortalidade', { params: { dias } })
      .then((r) => r.data.data),

  graficoQualidadeAgua: (dias = 30) =>
    apiClient
      .get<ApiResponse<QualidadeAguaPoint[]>>('/dashboard/grafico-qualidade-agua', { params: { dias } })
      .then((r) => r.data.data),

  graficoBiomassa: (dias = 90) =>
    apiClient
      .get<ApiResponse<TimeSeriesPoint[]>>('/dashboard/grafico-biomassa', { params: { dias } })
      .then((r) => r.data.data),

  producaoPorTanque: () =>
    apiClient
      .get<ApiResponse<ProducaoPorTanqueResponse[]>>('/dashboard/producao-por-tanque')
      .then((r) => r.data.data),
}

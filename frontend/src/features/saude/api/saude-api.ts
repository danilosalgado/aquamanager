import { apiClient } from '@/lib/api-client'
import type { ApiResponse } from '@/types/api'

export interface IndiceSaude {
  tanqueId: string
  score: number | null
  classificacao: 'EXCELENTE' | 'ATENCAO' | 'CRITICO' | null
  semDadosSuficientes: boolean
  detalhes: string[]
}

export const saudeApi = {
  atual: (tanqueId: string) =>
    apiClient.get<ApiResponse<IndiceSaude>>(`/tanques/${tanqueId}/indice-saude`).then((r) => r.data.data),
}

export const classificacaoConfig: Record<string, { label: string; emoji: string; className: string }> = {
  EXCELENTE: { label: 'Excelente', emoji: '🟢', className: 'text-success' },
  ATENCAO: { label: 'Atenção', emoji: '🟡', className: 'text-warning-foreground' },
  CRITICO: { label: 'Crítico', emoji: '🔴', className: 'text-destructive' },
}

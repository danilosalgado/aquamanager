import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Alimentacao {
  id: string
  loteId: string
  tipoRacao: string
  fornecedor: string | null
  quantidadeKg: number
  horario: string
  usuarioId: string
  custo: number | null
}

export interface AlimentacaoPayload {
  loteId: string
  tipoRacao: string
  fornecedor?: string | null
  quantidadeKg: number
  horario: string
  custo?: number | null
}

export const alimentacaoApi = {
  listar: (params: { loteId?: string; page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<Alimentacao>>>('/alimentacao', { params }).then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Alimentacao>>(`/alimentacao/${id}`).then((r) => r.data.data),

  criar: (payload: AlimentacaoPayload) =>
    apiClient.post<ApiResponse<Alimentacao>>('/alimentacao', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: AlimentacaoPayload) =>
    apiClient.put<ApiResponse<Alimentacao>>(`/alimentacao/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/alimentacao/${id}`),
}

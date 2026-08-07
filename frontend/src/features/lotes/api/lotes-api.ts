import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Lote {
  id: string
  tanqueId: string
  tanqueNome: string
  especieId: string
  especieNome: string
  fornecedor: string | null
  quantidadeInicial: number
  quantidadeAtual: number
  pesoInicialG: number
  pesoAtualG: number
  valorCompra: number | null
  dataCompra: string
  previsaoVenda: string | null
  status: 'ATIVO' | 'VENDIDO' | 'ENCERRADO'
  biomassaAtualKg: number
}

export interface LotePayload {
  tanqueId: string
  especieId: string
  fornecedor?: string | null
  quantidadeInicial: number
  pesoInicialG: number
  valorCompra?: number | null
  dataCompra: string
  previsaoVenda?: string | null
  status?: string
}

export const lotesApi = {
  listar: (params: { tanqueId?: string; status?: string; page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<Lote>>>('/lotes', { params }).then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Lote>>(`/lotes/${id}`).then((r) => r.data.data),

  criar: (payload: LotePayload) => apiClient.post<ApiResponse<Lote>>('/lotes', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: LotePayload) =>
    apiClient.put<ApiResponse<Lote>>(`/lotes/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/lotes/${id}`),
}

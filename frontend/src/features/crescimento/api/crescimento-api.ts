import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface RegistroCrescimento {
  id: string
  loteId: string
  pesoMedioG: number
  quantidadeAmostra: number
  biomassaKg: number
  dataPesagem: string
  usuarioId: string
}

export interface RegistroCrescimentoPayload {
  loteId: string
  pesoMedioG: number
  quantidadeAmostra: number
  dataPesagem: string
}

export const crescimentoApi = {
  listar: (params: { loteId?: string; page?: number; size?: number; sort?: string }) =>
    apiClient
      .get<ApiResponse<PageResponse<RegistroCrescimento>>>('/crescimento', { params })
      .then((r) => r.data.data),

  criar: (payload: RegistroCrescimentoPayload) =>
    apiClient.post<ApiResponse<RegistroCrescimento>>('/crescimento', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: RegistroCrescimentoPayload) =>
    apiClient.put<ApiResponse<RegistroCrescimento>>(`/crescimento/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/crescimento/${id}`),
}

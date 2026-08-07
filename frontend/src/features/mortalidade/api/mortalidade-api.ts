import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface RegistroMortalidade {
  id: string
  loteId: string
  quantidade: number
  data: string
  motivo: string
  observacoes: string | null
}

export interface RegistroMortalidadePayload {
  loteId: string
  quantidade: number
  data: string
  motivo: string
  observacoes?: string | null
}

export const mortalidadeApi = {
  listar: (params: { loteId?: string; page?: number; size?: number; sort?: string }) =>
    apiClient
      .get<ApiResponse<PageResponse<RegistroMortalidade>>>('/mortalidade', { params })
      .then((r) => r.data.data),

  criar: (payload: RegistroMortalidadePayload) =>
    apiClient.post<ApiResponse<RegistroMortalidade>>('/mortalidade', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: RegistroMortalidadePayload) =>
    apiClient.put<ApiResponse<RegistroMortalidade>>(`/mortalidade/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/mortalidade/${id}`),
}

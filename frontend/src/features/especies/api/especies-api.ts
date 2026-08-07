import { apiClient } from '@/lib/api-client'
import type { ApiResponse } from '@/types/api'

export interface Especie {
  id: string
  nome: string
  nomeCientifico: string | null
  cicloDiasPadrao: number | null
  pesoAbatePadraoG: number | null
  tempMin: number | null
  tempMax: number | null
  phMin: number | null
  phMax: number | null
  oxigenioMin: number | null
  amoniaMax: number | null
  nitritoMax: number | null
  ativo: boolean
  global: boolean
}

export interface EspeciePayload {
  nome: string
  nomeCientifico?: string | null
  cicloDiasPadrao?: number | null
  pesoAbatePadraoG?: number | null
  tempMin?: number | null
  tempMax?: number | null
  phMin?: number | null
  phMax?: number | null
  oxigenioMin?: number | null
  amoniaMax?: number | null
  nitritoMax?: number | null
  ativo?: boolean
}

export const especiesApi = {
  listar: () => apiClient.get<ApiResponse<Especie[]>>('/especies').then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Especie>>(`/especies/${id}`).then((r) => r.data.data),

  criar: (payload: EspeciePayload) =>
    apiClient.post<ApiResponse<Especie>>('/especies', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: EspeciePayload) =>
    apiClient.put<ApiResponse<Especie>>(`/especies/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/especies/${id}`),
}

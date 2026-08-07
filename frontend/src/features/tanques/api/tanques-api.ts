import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Tanque {
  id: string
  nome: string
  codigo: string
  tipo: 'ESCAVADO' | 'REDE' | 'CONCRETO' | 'BIOFLOCO'
  areaM2: number | null
  volumeM3: number | null
  profundidadeM: number | null
  capacidadeEstimadaKg: number | null
  latitude: number | null
  longitude: number | null
  status: 'ATIVO' | 'INATIVO' | 'MANUTENCAO'
  observacoes: string | null
  fotos: string[]
}

export interface TanquePayload {
  nome: string
  codigo: string
  tipo: string
  areaM2?: number | null
  volumeM3?: number | null
  profundidadeM?: number | null
  capacidadeEstimadaKg?: number | null
  latitude?: number | null
  longitude?: number | null
  status?: string
  observacoes?: string | null
  fotos?: string[]
}

export const tanquesApi = {
  listar: (params: { page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<Tanque>>>('/tanques', { params }).then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Tanque>>(`/tanques/${id}`).then((r) => r.data.data),

  criar: (payload: TanquePayload) =>
    apiClient.post<ApiResponse<Tanque>>('/tanques', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: TanquePayload) =>
    apiClient.put<ApiResponse<Tanque>>(`/tanques/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/tanques/${id}`),
}

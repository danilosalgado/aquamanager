import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Cliente {
  id: string
  nome: string
  documento: string | null
  telefone: string | null
  email: string | null
  endereco: string | null
  observacoes: string | null
}

export interface ClientePayload {
  nome: string
  documento?: string
  telefone?: string
  email?: string
  endereco?: string
  observacoes?: string
}

export const clientesApi = {
  listar: (params: { busca?: string; page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<Cliente>>>('/clientes', { params }).then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Cliente>>(`/clientes/${id}`).then((r) => r.data.data),

  criar: (payload: ClientePayload) =>
    apiClient.post<ApiResponse<Cliente>>('/clientes', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: ClientePayload) =>
    apiClient.put<ApiResponse<Cliente>>(`/clientes/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/clientes/${id}`),
}

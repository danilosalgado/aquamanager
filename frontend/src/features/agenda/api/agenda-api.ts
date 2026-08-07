import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { TipoEvento, EventoFormValues } from '../schemas/agenda-schema'

interface PaginationParams {
  page?: number
  size?: number
}

export interface Evento {
  id: string
  tipo: TipoEvento
  titulo: string
  descricao?: string
  dataInicio: string
  dataFim?: string
  concluido: boolean
  createdAt: string
}

export const agendaApi = {
  listar: (params?: PaginationParams) =>
    apiClient.get<ApiResponse<PageResponse<Evento>>>('/eventos', { params }).then((r) => r.data.data),

  listarPorPeriodo: (inicio: string, fim: string) =>
    apiClient
      .get<ApiResponse<Evento[]>>('/eventos/periodo', { params: { inicio, fim } })
      .then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Evento>>(`/eventos/${id}`).then((r) => r.data.data),

  criar: (data: EventoFormValues) =>
    apiClient.post<ApiResponse<Evento>>('/eventos', data).then((r) => r.data.data),

  atualizar: (id: string, data: EventoFormValues) =>
    apiClient.put<ApiResponse<Evento>>(`/eventos/${id}`, data).then((r) => r.data.data),

  alternarConcluido: (id: string) => apiClient.patch(`/eventos/${id}/concluir`),

  remover: (id: string) => apiClient.delete(`/eventos/${id}`),

  googleAuthUrl: () => apiClient.get<{ url: string }>('/integracoes/google/auth-url').then(r => r.data.url),
  
  googleStatus: () => apiClient.get<{ connected: boolean }>('/integracoes/google/status').then(r => r.data.connected),
}

import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Alerta {
  id: string
  tipo: string
  severidade: 'INFO' | 'ATENCAO' | 'CRITICO'
  titulo: string
  mensagem: string
  entidadeTipo: string | null
  entidadeId: string | null
  lido: boolean
  createdAt: string
}

export const alertasApi = {
  listar: (params: { naoLidos?: boolean; page?: number; size?: number }) =>
    apiClient
      .get<ApiResponse<PageResponse<Alerta>>>('/alertas', { params })
      .then((r) => r.data.data),

  contarNaoLidos: () =>
    apiClient.get<ApiResponse<{ total: number }>>('/alertas/contagem-nao-lidos').then((r) => r.data.data.total),

  marcarComoLido: (id: string) => apiClient.post(`/alertas/${id}/marcar-lido`),

  marcarTodosComoLidos: () => apiClient.post('/alertas/marcar-todos-lidos'),
}

import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface RegistroQualidadeAgua {
  id: string
  tanqueId: string
  temperatura: number | null
  ph: number | null
  oxigenioDissolvido: number | null
  amonia: number | null
  nitrito: number | null
  alcalinidade: number | null
  salinidade: number | null
  transparenciaCm: number | null
  medidoEm: string
  usuarioId: string
}

export interface RegistroQualidadeAguaPayload {
  tanqueId: string
  temperatura?: number | null
  ph?: number | null
  oxigenioDissolvido?: number | null
  amonia?: number | null
  nitrito?: number | null
  alcalinidade?: number | null
  salinidade?: number | null
  transparenciaCm?: number | null
  medidoEm: string
}

export const qualidadeAguaApi = {
  listar: (params: { tanqueId?: string; page?: number; size?: number; sort?: string }) =>
    apiClient
      .get<ApiResponse<PageResponse<RegistroQualidadeAgua>>>('/qualidade-agua', { params })
      .then((r) => r.data.data),

  criar: (payload: RegistroQualidadeAguaPayload) =>
    apiClient.post<ApiResponse<RegistroQualidadeAgua>>('/qualidade-agua', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: RegistroQualidadeAguaPayload) =>
    apiClient.put<ApiResponse<RegistroQualidadeAgua>>(`/qualidade-agua/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/qualidade-agua/${id}`),
}

import { apiClient } from '@/lib/api-client'
import { baixarBlob } from '@/lib/download'
import type { ApiResponse, ImportResultado, PageResponse } from '@/types/api'

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

  importar: async (arquivo: File): Promise<ImportResultado> => {
    const formData = new FormData()
    formData.append('arquivo', arquivo)
    const response = await apiClient.post<ApiResponse<ImportResultado>>('/mortalidade/importar', formData)
    return response.data.data
  },

  exportar: async () => {
    const response = await apiClient.get('/mortalidade/exportar', { responseType: 'blob' })
    baixarBlob(response, 'mortalidade.xlsx')
  },

  baixarModelo: async () => {
    const response = await apiClient.get('/mortalidade/importar/modelo', { responseType: 'blob' })
    baixarBlob(response, 'modelo-mortalidade.xlsx')
  },
}

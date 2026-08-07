import { apiClient } from '@/lib/api-client'
import { baixarBlob } from '@/lib/download'
import type { ApiResponse, ImportResultado, PageResponse } from '@/types/api'

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

  importar: async (arquivo: File): Promise<ImportResultado> => {
    const formData = new FormData()
    formData.append('arquivo', arquivo)
    const response = await apiClient.post<ApiResponse<ImportResultado>>('/crescimento/importar', formData)
    return response.data.data
  },

  exportar: async () => {
    const response = await apiClient.get('/crescimento/exportar', { responseType: 'blob' })
    baixarBlob(response, 'crescimento.xlsx')
  },

  baixarModelo: async () => {
    const response = await apiClient.get('/crescimento/importar/modelo', { responseType: 'blob' })
    baixarBlob(response, 'modelo-crescimento.xlsx')
  },
}

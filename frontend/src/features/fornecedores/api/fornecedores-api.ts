import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Fornecedor {
  id: string
  nome: string
  documento: string | null
  telefone: string | null
  email: string | null
  produtosFornecidos: string | null
  observacoes: string | null
}

export interface FornecedorPayload {
  nome: string
  documento?: string
  telefone?: string
  email?: string
  produtosFornecidos?: string
  observacoes?: string
}

export const fornecedoresApi = {
  listar: (params: { busca?: string; page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<Fornecedor>>>('/fornecedores', { params }).then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Fornecedor>>(`/fornecedores/${id}`).then((r) => r.data.data),

  criar: (payload: FornecedorPayload) =>
    apiClient.post<ApiResponse<Fornecedor>>('/fornecedores', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: FornecedorPayload) =>
    apiClient.put<ApiResponse<Fornecedor>>(`/fornecedores/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/fornecedores/${id}`),
}

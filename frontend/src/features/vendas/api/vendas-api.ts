import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Venda {
  id: string
  clienteId: string | null
  clienteNome: string | null
  categoriaProduto: string
  quantidadeKg: number
  valorTotal: number
  dataVenda: string
  observacoes: string | null
}

export interface VendaPayload {
  categoriaProduto: string
  quantidadeKg: number
  valorTotal: number
  dataVenda: string
  clienteId?: string | null
  observacoes?: string | null
}

export interface ResumoLucroBruto {
  receita: number
  custoRacao: number
  custoOperacional: number
  lucroBruto: number
}

export const vendasApi = {
  listar: (params: { categoriaProduto?: string; page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<Venda>>>('/vendas', { params }).then((r) => r.data.data),

  buscar: (id: string) => apiClient.get<ApiResponse<Venda>>(`/vendas/${id}`).then((r) => r.data.data),

  criar: (payload: VendaPayload) => apiClient.post<ApiResponse<Venda>>('/vendas', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: VendaPayload) =>
    apiClient.put<ApiResponse<Venda>>(`/vendas/${id}`, payload).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/vendas/${id}`),

  categorias: () => apiClient.get<ApiResponse<string[]>>('/vendas/categorias').then((r) => r.data.data),

  resumoLucroBruto: () =>
    apiClient.get<ApiResponse<ResumoLucroBruto>>('/vendas/relatorio/lucro-bruto').then((r) => r.data.data),
}

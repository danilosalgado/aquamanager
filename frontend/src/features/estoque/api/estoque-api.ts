import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface EstoqueItem {
  id: string
  categoria: 'RACAO' | 'MEDICAMENTO' | 'QUIMICO' | 'EQUIPAMENTO' | 'MATERIAL'
  nome: string
  unidade: string
  quantidadeAtual: number
  quantidadeMinima: number | null
  fornecedorId: string | null
  fornecedorNome: string | null
  validade: string | null
  precoUnitario: number | null
}

export interface EstoqueItemPayload {
  categoria: string
  nome: string
  unidade: string
  quantidadeMinima?: number | null
  fornecedorId?: string | null
  validade?: string | null
  precoUnitario?: number | null
}

export interface Movimentacao {
  id: string
  itemId: string
  itemNome: string
  tipo: 'ENTRADA' | 'SAIDA'
  quantidade: number
  motivo: string | null
  usuarioId: string
  createdAt: string
}

export interface MovimentacaoPayload {
  itemId: string
  tipo: string
  quantidade: number
  motivo?: string
}

export const estoqueApi = {
  listarItens: (params: { page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<EstoqueItem>>>('/estoque/itens', { params }).then((r) => r.data.data),

  criarItem: (payload: EstoqueItemPayload) =>
    apiClient.post<ApiResponse<EstoqueItem>>('/estoque/itens', payload).then((r) => r.data.data),

  atualizarItem: (id: string, payload: EstoqueItemPayload) =>
    apiClient.put<ApiResponse<EstoqueItem>>(`/estoque/itens/${id}`, payload).then((r) => r.data.data),

  removerItem: (id: string) => apiClient.delete(`/estoque/itens/${id}`),

  listarMovimentacoes: (params: { itemId?: string; page?: number; size?: number }) =>
    apiClient
      .get<ApiResponse<PageResponse<Movimentacao>>>('/estoque/movimentacoes', { params })
      .then((r) => r.data.data),

  registrarMovimentacao: (payload: MovimentacaoPayload) =>
    apiClient.post<ApiResponse<Movimentacao>>('/estoque/movimentacoes', payload).then((r) => r.data.data),
}

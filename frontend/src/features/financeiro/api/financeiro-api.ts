import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'

export interface Lancamento {
  id: string
  tipo: 'RECEITA' | 'DESPESA'
  categoria: string
  descricao: string
  valor: number
  dataVencimento: string
  dataPagamento: string | null
  status: 'PENDENTE' | 'PAGO' | 'ATRASADO' | 'CANCELADO'
  formaPagamento: string | null
  clienteId: string | null
  clienteNome: string | null
  fornecedorId: string | null
  fornecedorNome: string | null
  loteId: string | null
}

export interface LancamentoPayload {
  tipo: string
  categoria: string
  descricao: string
  valor: number
  dataVencimento: string
  formaPagamento?: string | null
  clienteId?: string | null
  fornecedorId?: string | null
  loteId?: string | null
}

export interface ResumoFinanceiro {
  receitaTotal: number
  despesaTotal: number
  lucro: number
  margemPercentual: number
  receitaPendente: number
  despesaPendente: number
}

export const financeiroApi = {
  listar: (params: { tipo?: string; status?: string; page?: number; size?: number }) =>
    apiClient
      .get<ApiResponse<PageResponse<Lancamento>>>('/financeiro/lancamentos', { params })
      .then((r) => r.data.data),

  criar: (payload: LancamentoPayload) =>
    apiClient.post<ApiResponse<Lancamento>>('/financeiro/lancamentos', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: LancamentoPayload) =>
    apiClient.put<ApiResponse<Lancamento>>(`/financeiro/lancamentos/${id}`, payload).then((r) => r.data.data),

  marcarComoPago: (id: string) =>
    apiClient.post<ApiResponse<Lancamento>>(`/financeiro/lancamentos/${id}/pagar`).then((r) => r.data.data),

  remover: (id: string) => apiClient.delete(`/financeiro/lancamentos/${id}`),

  resumo: (inicio: string, fim: string) =>
    apiClient
      .get<ApiResponse<ResumoFinanceiro>>('/financeiro/resumo', { params: { inicio, fim } })
      .then((r) => r.data.data),
}

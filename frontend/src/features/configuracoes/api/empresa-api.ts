import { apiClient } from '@/lib/api-client'
import type { ApiResponse } from '@/types/api'

export type EmpresaStatus = 'TRIAL' | 'ATIVA' | 'INADIMPLENTE' | 'CANCELADA' | 'BLOQUEADA'

export interface Plano {
  id: string
  codigo: string
  nome: string
  precoMensal: number
  limiteTanques: number | null
  limiteUsuarios: number | null
  recursos: Record<string, boolean>
}

export interface Empresa {
  id: string
  nome: string
  documento: string
  endereco: string | null
  cidade: string | null
  estado: string | null
  telefone: string | null
  email: string | null
  status: EmpresaStatus
  trialEndsAt: string | null
  isentoCobranca: boolean
  plano: Plano
}

export interface EmpresaUpdatePayload {
  nome: string
  endereco?: string
  cidade?: string
  estado?: string
  telefone?: string
  email?: string
}

export interface Assinatura {
  id: string
  status: string
  dataInicio: string
  proximoVencimento: string | null
  plano: Plano
}

export const empresaApi = {
  buscarMinhaEmpresa: () => apiClient.get<ApiResponse<Empresa>>('/empresas/me').then((r) => r.data.data),

  atualizar: (payload: EmpresaUpdatePayload) =>
    apiClient.put<ApiResponse<Empresa>>('/empresas/me', payload).then((r) => r.data.data),
}

export const billingApi = {
  assinaturaAtual: () => apiClient.get<ApiResponse<Assinatura>>('/billing/assinatura').then((r) => r.data.data),

  alterarPlano: (codigoPlano: string) =>
    apiClient.post<ApiResponse<Assinatura>>('/billing/plano', { codigoPlano }).then((r) => r.data.data),

  cancelar: () => apiClient.delete('/billing/assinatura'),

  criarCheckoutSession: () =>
    apiClient
      .post<ApiResponse<{ checkoutUrl: string }>>('/billing/checkout-session')
      .then((r) => r.data.data.checkoutUrl),

  abrirPortal: () =>
    apiClient.post<ApiResponse<{ url: string }>>('/billing/portal-session').then((r) => r.data.data.url),
}

export const planosApi = {
  listar: () => apiClient.get<ApiResponse<Plano[]>>('/planos').then((r) => r.data.data),
}

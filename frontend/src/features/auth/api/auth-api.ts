import { apiClient } from '@/lib/api-client'
import type { ApiResponse } from '@/types/api'
import type { LoginResponse, Usuario } from '@/types/auth'

export interface RegisterPayload {
  nomeEmpresa: string
  documento: string
  endereco?: string
  cidade?: string
  estado?: string
  telefone?: string
  emailEmpresa: string
  nomeUsuario: string
  emailUsuario: string
  senha: string
}

export interface LoginPayload {
  email: string
  senha: string
  codigo2fa?: string
}

export const authApi = {
  register: (payload: RegisterPayload) =>
    apiClient.post<ApiResponse<LoginResponse>>('/auth/register', payload).then((r) => r.data.data),

  login: (payload: LoginPayload) =>
    apiClient.post<ApiResponse<LoginResponse>>('/auth/login', payload).then((r) => r.data.data),

  refresh: () => apiClient.post<ApiResponse<LoginResponse>>('/auth/refresh').then((r) => r.data.data),

  logout: () => apiClient.post('/auth/logout'),

  me: () => apiClient.get<ApiResponse<Usuario>>('/auth/me').then((r) => r.data.data),

  forgotPassword: (email: string) => apiClient.post('/auth/forgot-password', { email }),

  resetPassword: (token: string, novaSenha: string) => apiClient.post('/auth/reset-password', { token, novaSenha }),

  changePassword: (senhaAtual: string, novaSenha: string) =>
    apiClient.post('/auth/change-password', { senhaAtual, novaSenha }),

  confirmEmail: (token: string) => apiClient.get('/auth/confirm-email', { params: { token } }),

  resendConfirmation: () => apiClient.post('/auth/resend-confirmation'),

  setup2Fa: () =>
    apiClient
      .post<ApiResponse<{ secret: string; qrCodeDataUri: string }>>('/auth/2fa/setup')
      .then((r) => r.data.data),

  enable2Fa: (codigo: string) => apiClient.post('/auth/2fa/enable', { codigo }),

  disable2Fa: (senhaAtual: string) => apiClient.delete('/auth/2fa', { data: { senhaAtual } }),
}

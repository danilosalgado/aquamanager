import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { Role, Usuario } from '@/types/auth'

export interface UsuarioCreatePayload {
  nome: string
  email: string
  senha: string
  role: Role
}

export interface UsuarioUpdatePayload {
  nome: string
  role: Role
  ativo: boolean
}

export const usuariosApi = {
  listar: (params: { busca?: string; page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<Usuario>>>('/usuarios', { params }).then((r) => r.data.data),

  criar: (payload: UsuarioCreatePayload) =>
    apiClient.post<ApiResponse<Usuario>>('/usuarios', payload).then((r) => r.data.data),

  atualizar: (id: string, payload: UsuarioUpdatePayload) =>
    apiClient.put<ApiResponse<Usuario>>(`/usuarios/${id}`, payload).then((r) => r.data.data),

  desativar: (id: string) => apiClient.delete(`/usuarios/${id}`),
}

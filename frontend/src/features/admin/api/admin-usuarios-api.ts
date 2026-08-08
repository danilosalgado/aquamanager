import { apiClient } from '@/lib/api-client'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { Role } from '@/types/auth'

export interface UsuarioAdmin {
  id: string
  nome: string
  email: string
  role: Role
  ativo: boolean
  empresaId: string
  empresaNome: string
}

export interface UsuarioAdminUpdatePayload {
  nome: string
  role: Role
  ativo: boolean
}

export const adminUsuariosApi = {
  listar: (params: { page?: number; size?: number }) =>
    apiClient.get<ApiResponse<PageResponse<UsuarioAdmin>>>('/admin/usuarios', { params }).then((r) => r.data.data),

  atualizar: (id: string, payload: UsuarioAdminUpdatePayload) =>
    apiClient.put<ApiResponse<UsuarioAdmin>>(`/admin/usuarios/${id}`, payload).then((r) => r.data.data),

  desativar: (id: string) => apiClient.delete(`/admin/usuarios/${id}`),
}

export type Role = 'ADMINISTRADOR' | 'GERENTE' | 'FUNCIONARIO' | 'CONSULTOR'

export interface Usuario {
  id: string
  nome: string
  email: string
  role: Role
  ativo: boolean
  emailConfirmado: boolean
  twoFactorEnabled: boolean
  empresaId: string
}

export interface LoginResponse {
  accessToken: string
  expiresIn: number
  usuario: Usuario
}

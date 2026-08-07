import { create } from 'zustand'
import type { Usuario } from '@/types/auth'

interface AuthState {
  accessToken: string | null
  usuario: Usuario | null
  isBootstrapping: boolean
  setSession: (accessToken: string, usuario: Usuario) => void
  clearSession: () => void
  setBootstrapping: (value: boolean) => void
}

/**
 * O access token vive apenas em memória (nunca em localStorage) — mitigação de XSS.
 * O refresh token é um cookie httpOnly, inacessível ao JavaScript. Ao recarregar a
 * página, o app chama POST /auth/refresh silenciosamente para obter um novo access
 * token a partir do cookie (ver AppBootstrap).
 */
export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  usuario: null,
  isBootstrapping: true,
  setSession: (accessToken, usuario) => set({ accessToken, usuario }),
  clearSession: () => set({ accessToken: null, usuario: null }),
  setBootstrapping: (value) => set({ isBootstrapping: value }),
}))

export function hasRole(usuario: Usuario | null, ...roles: Usuario['role'][]): boolean {
  if (!usuario) return false
  return roles.includes(usuario.role)
}

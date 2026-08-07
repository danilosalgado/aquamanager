import { useEffect } from 'react'
import { useAuthStore, hasRole } from '@/stores/auth-store'
import { refreshAccessToken } from '@/lib/api-client'
import type { Role } from '@/types/auth'

/** Tenta restaurar a sessão a partir do cookie httpOnly de refresh ao carregar o app. */
export function useAuthBootstrap() {
  const setBootstrapping = useAuthStore((s) => s.setBootstrapping)

  useEffect(() => {
    refreshAccessToken().finally(() => setBootstrapping(false))
  }, [setBootstrapping])
}

export function useAuth() {
  const accessToken = useAuthStore((s) => s.accessToken)
  const usuario = useAuthStore((s) => s.usuario)
  const isBootstrapping = useAuthStore((s) => s.isBootstrapping)
  const clearSession = useAuthStore((s) => s.clearSession)

  return {
    usuario,
    isAuthenticated: !!accessToken && !!usuario,
    isBootstrapping,
    logout: clearSession,
    hasRole: (...roles: Role[]) => hasRole(usuario, ...roles),
  }
}

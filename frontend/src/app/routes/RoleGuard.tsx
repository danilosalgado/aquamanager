import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/hooks/use-auth'
import type { Role } from '@/types/auth'

export function RoleGuard({ roles }: { roles: Role[] }) {
  const { hasRole } = useAuth()
  if (!hasRole(...roles)) {
    return <Navigate to="/" replace />
  }
  return <Outlet />
}

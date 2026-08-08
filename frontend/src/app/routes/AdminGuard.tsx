import { Navigate, Outlet } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { empresaApi } from '@/features/configuracoes/api/empresa-api'

export function AdminGuard() {
  const { data: empresa, isLoading } = useQuery({
    queryKey: ['empresa', 'me'],
    queryFn: () => empresaApi.buscarMinhaEmpresa(),
  })

  if (isLoading) return null
  if (!empresa?.isentoCobranca) {
    return <Navigate to="/" replace />
  }
  return <Outlet />
}

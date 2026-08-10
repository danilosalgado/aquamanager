import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { ProtectedRoute } from './ProtectedRoute'
import { RoleGuard } from './RoleGuard'
import { AdminGuard } from './AdminGuard'
import { AppShell } from '@/app/layout/AppShell'

const LoginPage = lazy(() => import('@/features/auth/pages/LoginPage'))
const RegisterPage = lazy(() => import('@/features/auth/pages/RegisterPage'))
const ForgotPasswordPage = lazy(() => import('@/features/auth/pages/ForgotPasswordPage'))
const ResetPasswordPage = lazy(() => import('@/features/auth/pages/ResetPasswordPage'))
const ConfirmEmailPage = lazy(() => import('@/features/auth/pages/ConfirmEmailPage'))

const DashboardPage = lazy(() => import('@/features/dashboard/pages/DashboardPage'))
const AlertasPage = lazy(() => import('@/features/alertas/pages/AlertasPage'))
const AgendaPage = lazy(() => import('@/features/agenda/pages/AgendaPage'))
const RelatoriosPage = lazy(() => import('@/features/relatorios/pages/RelatoriosPage'))
const TanquesListPage = lazy(() => import('@/features/tanques/pages/TanquesListPage'))
const TanqueDetailPage = lazy(() => import('@/features/tanques/pages/TanqueDetailPage'))
const LotesListPage = lazy(() => import('@/features/lotes/pages/LotesListPage'))
const LoteDetailPage = lazy(() => import('@/features/lotes/pages/LoteDetailPage'))
const EspeciesListPage = lazy(() => import('@/features/especies/pages/EspeciesListPage'))
const AlimentacaoListPage = lazy(() => import('@/features/alimentacao/pages/AlimentacaoListPage'))
const QualidadeAguaListPage = lazy(() => import('@/features/qualidade-agua/pages/QualidadeAguaListPage'))
const CrescimentoListPage = lazy(() => import('@/features/crescimento/pages/CrescimentoListPage'))
const MortalidadeListPage = lazy(() => import('@/features/mortalidade/pages/MortalidadeListPage'))
const EstoqueListPage = lazy(() => import('@/features/estoque/pages/EstoqueListPage'))
const FinanceiroListPage = lazy(() => import('@/features/financeiro/pages/FinanceiroListPage'))
const ClientesListPage = lazy(() => import('@/features/clientes/pages/ClientesListPage'))
const FornecedoresListPage = lazy(() => import('@/features/fornecedores/pages/FornecedoresListPage'))
const UsuariosListPage = lazy(() => import('@/features/usuarios/pages/UsuariosListPage'))
const AdminUsuariosListPage = lazy(() => import('@/features/admin/pages/AdminUsuariosListPage'))
const ConfiguracoesPage = lazy(() => import('@/features/configuracoes/pages/ConfiguracoesPage'))
const PerfilPage = lazy(() => import('@/features/configuracoes/pages/PerfilPage'))

function PageFallback() {
  return (
    <div className="flex h-64 items-center justify-center">
      <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
    </div>
  )
}

export function AppRoutes() {
  return (
    <Suspense fallback={<PageFallback />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<RegisterPage />} />
        <Route path="/esqueci-senha" element={<ForgotPasswordPage />} />
        <Route path="/redefinir-senha" element={<ResetPasswordPage />} />
        <Route path="/confirmar-email" element={<ConfirmEmailPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route index element={<DashboardPage />} />
            <Route path="/alertas" element={<AlertasPage />} />
            <Route path="/agenda" element={<AgendaPage />} />
            <Route path="/relatorios" element={<RelatoriosPage />} />
            <Route path="/tanques" element={<TanquesListPage />} />
            <Route path="/tanques/:id" element={<TanqueDetailPage />} />
            <Route path="/lotes" element={<LotesListPage />} />
            <Route path="/lotes/:id" element={<LoteDetailPage />} />
            <Route path="/especies" element={<EspeciesListPage />} />
            <Route path="/alimentacao" element={<AlimentacaoListPage />} />
            <Route path="/qualidade-agua" element={<QualidadeAguaListPage />} />
            <Route path="/crescimento" element={<CrescimentoListPage />} />
            <Route path="/mortalidade" element={<MortalidadeListPage />} />
            <Route path="/estoque" element={<EstoqueListPage />} />
            <Route path="/clientes" element={<ClientesListPage />} />
            <Route path="/fornecedores" element={<FornecedoresListPage />} />
            <Route path="/perfil" element={<PerfilPage />} />

            <Route element={<RoleGuard roles={['ADMINISTRADOR', 'GERENTE', 'CONSULTOR']} />}>
              <Route path="/financeiro" element={<FinanceiroListPage />} />
            </Route>
            <Route element={<RoleGuard roles={['ADMINISTRADOR', 'GERENTE']} />}>
              <Route path="/usuarios" element={<UsuariosListPage />} />
            </Route>
            <Route element={<RoleGuard roles={['ADMINISTRADOR']} />}>
              <Route path="/configuracoes" element={<ConfiguracoesPage />} />
              <Route element={<AdminGuard />}>
                <Route path="/admin/usuarios" element={<AdminUsuariosListPage />} />
              </Route>
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  )
}

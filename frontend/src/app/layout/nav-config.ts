import {
  LayoutDashboard, Waves, Layers, Fish, Utensils, Droplets, TrendingUp,
  Skull, Boxes, Wallet, Users, Truck, UserCog, Bell, Building2, Calendar, FileText, Target,
} from 'lucide-react'
import type { Role } from '@/types/auth'

export interface NavItem {
  label: string
  to: string
  icon: typeof LayoutDashboard
  roles?: Role[]
  /** Só aparece para contas administrativas da plataforma (empresa.isentoCobranca). */
  adminOnly?: boolean
}

export interface NavGroup {
  title: string
  items: NavItem[]
}

export const navGroups: NavGroup[] = [
  {
    title: 'Visão geral',
    items: [
      { label: 'Dashboard', to: '/', icon: LayoutDashboard },
      { label: 'Alertas', to: '/alertas', icon: Bell },
      { label: 'Agenda', to: '/agenda', icon: Calendar },
      { label: 'Relatórios', to: '/relatorios', icon: FileText },
    ],
  },
  {
    title: 'Produção',
    items: [
      { label: 'Tanques', to: '/tanques', icon: Waves },
      { label: 'Lotes', to: '/lotes', icon: Layers },
      { label: 'Crescimento Potencial', to: '/crescimento-potencial', icon: Target },
      { label: 'Espécies', to: '/especies', icon: Fish },
      { label: 'Alimentação', to: '/alimentacao', icon: Utensils },
      { label: 'Qualidade da água', to: '/qualidade-agua', icon: Droplets },
      { label: 'Crescimento', to: '/crescimento', icon: TrendingUp },
      { label: 'Mortalidade', to: '/mortalidade', icon: Skull },
    ],
  },
  {
    title: 'Gestão',
    items: [
      { label: 'Estoque', to: '/estoque', icon: Boxes },
      { label: 'Financeiro', to: '/financeiro', icon: Wallet, roles: ['ADMINISTRADOR', 'GERENTE', 'CONSULTOR'] },
      { label: 'Clientes', to: '/clientes', icon: Users },
      { label: 'Fornecedores', to: '/fornecedores', icon: Truck },
    ],
  },
  {
    title: 'Empresa',
    items: [
      { label: 'Usuários', to: '/admin/usuarios', icon: UserCog, adminOnly: true },
      { label: 'Empresa & Plano', to: '/configuracoes', icon: Building2, roles: ['ADMINISTRADOR'] },
    ],
  },
]

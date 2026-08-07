import { NavLink } from 'react-router-dom'
import { Waves } from 'lucide-react'
import { cn } from '@/lib/utils'
import { navGroups } from './nav-config'
import { useAuth } from '@/hooks/use-auth'

export function Sidebar({ className }: { className?: string }) {
  const { hasRole } = useAuth()

  return (
    <aside className={cn('flex h-full w-64 flex-col border-r border-border bg-card/50', className)}>
      <div className="flex h-14 items-center gap-2 border-b border-border px-5">
        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-primary text-primary-foreground">
          <Waves className="h-4 w-4" />
        </div>
        <span className="text-sm font-semibold tracking-tight">AquaManager</span>
      </div>

      <nav className="flex-1 overflow-y-auto scrollbar-thin px-3 py-4">
        {navGroups.map((group) => (
          <div key={group.title} className="mb-5">
            <p className="mb-1.5 px-2 text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
              {group.title}
            </p>
            <div className="space-y-0.5">
              {group.items
                .filter((item) => !item.roles || hasRole(...item.roles))
                .map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.to === '/'}
                    className={({ isActive }) =>
                      cn(
                        'flex items-center gap-2.5 rounded-md px-2.5 py-1.5 text-sm font-medium transition-colors',
                        isActive
                          ? 'bg-primary/10 text-primary'
                          : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground',
                      )
                    }
                  >
                    <item.icon className="h-4 w-4 shrink-0" />
                    {item.label}
                  </NavLink>
                ))}
            </div>
          </div>
        ))}
      </nav>
    </aside>
  )
}

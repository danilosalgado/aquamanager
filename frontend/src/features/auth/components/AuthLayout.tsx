import { Waves } from 'lucide-react'
import { motion } from 'framer-motion'
import type { ReactNode } from 'react'

export function AuthLayout({
  title,
  description,
  children,
  wide,
}: {
  title: string
  description?: string
  children: ReactNode
  wide?: boolean
}) {
  return (
    <div className="grid min-h-dvh lg:grid-cols-2">
      <div className="relative hidden flex-col justify-between overflow-hidden bg-primary p-10 text-primary-foreground lg:flex">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.15),transparent_60%)]" />
        <div className="relative flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/15">
            <Waves className="h-4.5 w-4.5" />
          </div>
          <span className="text-lg font-semibold">AquaManager</span>
        </div>
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="relative max-w-md"
        >
          <p className="text-2xl font-medium leading-snug">
            Gestão completa da sua piscicultura, do tanque à venda.
          </p>
          <p className="mt-3 text-sm text-primary-foreground/80">
            Controle produção, água, financeiro e equipe em uma única plataforma feita para o
            piscicultor brasileiro.
          </p>
        </motion.div>
        <p className="relative text-xs text-primary-foreground/60">
          © {new Date().getFullYear()} AquaManager. Todos os direitos reservados.
        </p>
      </div>

      <div className="flex items-center justify-center p-6 sm:p-10">
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className={wide ? 'w-full max-w-lg' : 'w-full max-w-sm'}
        >
          <div className="mb-2 flex items-center gap-2 lg:hidden">
            <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <Waves className="h-4 w-4" />
            </div>
            <span className="text-sm font-semibold">AquaManager</span>
          </div>
          <h1 className="text-xl font-semibold tracking-tight">{title}</h1>
          {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
          <div className="mt-6">{children}</div>
        </motion.div>
      </div>
    </div>
  )
}

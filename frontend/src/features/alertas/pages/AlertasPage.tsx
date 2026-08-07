import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Bell, BellOff, Check, CheckCheck } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { cn, formatDateTime } from '@/lib/utils'
import { alertasApi } from '../api/alertas-api'

const severidadeConfig = {
  INFO: { label: 'Info', className: 'bg-secondary text-secondary-foreground' },
  ATENCAO: { label: 'Atenção', className: 'bg-warning/15 text-warning-foreground' },
  CRITICO: { label: 'Crítico', className: 'bg-destructive/15 text-destructive' },
} as const

export default function AlertasPage() {
  const [page, setPage] = useState(0)
  const [apenasNaoLidos, setApenasNaoLidos] = useState(false)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['alertas', page, apenasNaoLidos],
    queryFn: () => alertasApi.listar({ page, size: 15, naoLidos: apenasNaoLidos || undefined }),
  })

  const marcarLido = useMutation({
    mutationFn: (id: string) => alertasApi.marcarComoLido(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alertas'] }),
  })

  const marcarTodos = useMutation({
    mutationFn: alertasApi.marcarTodosComoLidos,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alertas'] }),
  })

  return (
    <div>
      <PageHeader
        title="Alertas"
        description="Notificações automáticas geradas pelo sistema com base nos seus dados."
        actions={
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={() => setApenasNaoLidos((v) => !v)}>
              {apenasNaoLidos ? <Bell className="h-4 w-4" /> : <BellOff className="h-4 w-4" />}
              {apenasNaoLidos ? 'Mostrando não lidos' : 'Mostrar todos'}
            </Button>
            <Button size="sm" onClick={() => marcarTodos.mutate()} loading={marcarTodos.isPending}>
              <CheckCheck className="h-4 w-4" /> Marcar todos como lidos
            </Button>
          </div>
        }
      />

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-20 w-full" />
          ))}
        </div>
      ) : !data?.content.length ? (
        <EmptyState icon={Bell} title="Nenhum alerta por aqui" description="Você está em dia! Novos alertas aparecerão aqui automaticamente." />
      ) : (
        <div className="space-y-2">
          {data.content.map((alerta) => {
            const cfg = severidadeConfig[alerta.severidade]
            return (
              <Card
                key={alerta.id}
                className={cn('flex items-start justify-between gap-3 p-4', !alerta.lido && 'border-primary/30 bg-primary/[0.03]')}
              >
                <div className="min-w-0 flex-1">
                  <div className="mb-1 flex items-center gap-2">
                    <Badge className={cfg.className} variant="outline">
                      {cfg.label}
                    </Badge>
                    {!alerta.lido && <span className="h-1.5 w-1.5 rounded-full bg-primary" />}
                  </div>
                  <p className="text-sm font-medium">{alerta.titulo}</p>
                  <p className="mt-0.5 text-sm text-muted-foreground">{alerta.mensagem}</p>
                  <p className="mt-1 text-xs text-muted-foreground">{formatDateTime(alerta.createdAt)}</p>
                </div>
                {!alerta.lido && (
                  <Button variant="ghost" size="icon" className="h-8 w-8 shrink-0" onClick={() => marcarLido.mutate(alerta.id)}>
                    <Check className="h-4 w-4" />
                  </Button>
                )}
              </Card>
            )
          })}
          <DataTablePagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onPageChange={setPage} />
        </div>
      )}
    </div>
  )
}

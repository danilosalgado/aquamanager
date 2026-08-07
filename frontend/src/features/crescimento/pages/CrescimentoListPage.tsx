import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, TrendingUp, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { crescimentoApi, type RegistroCrescimento } from '../api/crescimento-api'
import { CrescimentoFormDialog } from '../components/CrescimentoFormDialog'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'
import { formatDate, formatNumber } from '@/lib/utils'

export default function CrescimentoListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<RegistroCrescimento | null>(null)
  const [removing, setRemoving] = useState<RegistroCrescimento | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['crescimento', page],
    queryFn: () => crescimentoApi.listar({ page, size: 10, sort: 'dataPesagem,desc' }),
  })

  const { data: lotes } = useQuery({
    queryKey: ['lotes', 'select'],
    queryFn: () => lotesApi.listar({ size: 100 }),
  })

  const loteNomeMap = new Map((lotes?.content ?? []).map((l) => [l.id, `${l.tanqueNome} / ${l.especieNome}`]))

  const deleteMutation = useMutation({
    mutationFn: (id: string) => crescimentoApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['crescimento'] })
      toast.success('Registro removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o registro.')),
  })

  return (
    <div>
      <PageHeader
        title="Crescimento"
        description="Acompanhamento de biometria e ganho de peso dos lotes."
        actions={
          podeGerenciar && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Novo registro
            </Button>
          )
        }
      />

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : !data?.content.length ? (
        <EmptyState
          icon={TrendingUp}
          title="Nenhum registro de crescimento"
          description="Registre biometrias periódicas para acompanhar o ganho de peso dos lotes."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Novo registro
              </Button>
            )
          }
        />
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Data</TableHead>
                <TableHead>Lote</TableHead>
                <TableHead>Peso médio (g)</TableHead>
                <TableHead>Biomassa (kg)</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((registro) => (
                <TableRow key={registro.id}>
                  <TableCell className="font-medium">{formatDate(registro.dataPesagem)}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {loteNomeMap.get(registro.loteId) ?? '—'}
                  </TableCell>
                  <TableCell>{formatNumber(registro.pesoMedioG, 2)}</TableCell>
                  <TableCell>{formatNumber(registro.biomassaKg, 3)}</TableCell>
                  <TableCell>
                    {podeGerenciar && (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => { setEditing(registro); setFormOpen(true) }}>
                            <Pencil className="mr-2 h-4 w-4" /> Editar
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setRemoving(registro)}
                          >
                            <Trash2 className="mr-2 h-4 w-4" /> Remover
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <DataTablePagination
            page={data.page}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            onPageChange={setPage}
          />
        </div>
      )}

      <CrescimentoFormDialog open={formOpen} onOpenChange={setFormOpen} registro={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title="Remover registro de crescimento?"
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

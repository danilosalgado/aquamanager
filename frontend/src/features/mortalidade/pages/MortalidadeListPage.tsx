import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Skull, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
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
import { mortalidadeApi, type RegistroMortalidade } from '../api/mortalidade-api'
import { MortalidadeFormDialog } from '../components/MortalidadeFormDialog'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'
import { cn, formatDate, formatNumber } from '@/lib/utils'

const LIMITE_QUANTIDADE_ALTA = 50

export default function MortalidadeListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<RegistroMortalidade | null>(null)
  const [removing, setRemoving] = useState<RegistroMortalidade | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['mortalidade', page],
    queryFn: () => mortalidadeApi.listar({ page, size: 10, sort: 'data,desc' }),
  })

  const { data: lotes } = useQuery({
    queryKey: ['lotes', 'select'],
    queryFn: () => lotesApi.listar({ size: 100 }),
  })

  const loteNomeMap = new Map((lotes?.content ?? []).map((l) => [l.id, `${l.tanqueNome} / ${l.especieNome}`]))

  const deleteMutation = useMutation({
    mutationFn: (id: string) => mortalidadeApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mortalidade'] })
      toast.success('Registro removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o registro.')),
  })

  return (
    <div>
      <PageHeader
        title="Mortalidade"
        description="Registros de perdas por lote para acompanhamento da sobrevivência."
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
          icon={Skull}
          title="Nenhum registro de mortalidade"
          description="Registre as perdas dos seus lotes para acompanhar a taxa de sobrevivência."
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
                <TableHead>Quantidade</TableHead>
                <TableHead>Motivo</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((registro) => (
                <TableRow key={registro.id}>
                  <TableCell className="font-medium">{formatDate(registro.data)}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {loteNomeMap.get(registro.loteId) ?? '—'}
                  </TableCell>
                  <TableCell
                    className={cn(
                      registro.quantidade >= LIMITE_QUANTIDADE_ALTA && 'font-semibold text-destructive',
                    )}
                  >
                    {formatNumber(registro.quantidade)}
                  </TableCell>
                  <TableCell className="text-muted-foreground">{registro.motivo}</TableCell>
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

      <MortalidadeFormDialog open={formOpen} onOpenChange={setFormOpen} registro={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title="Remover registro de mortalidade?"
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

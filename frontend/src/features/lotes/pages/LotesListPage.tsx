import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Layers, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { lotesApi, type Lote } from '../api/lotes-api'
import { statusLoteLabels } from '../schemas/lote-schema'
import { LoteFormDialog } from '../components/LoteFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { formatNumber } from '@/lib/utils'
import { useAuth } from '@/hooks/use-auth'

export default function LotesListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE')
  const [page, setPage] = useState(0)
  const [statusFiltro, setStatusFiltro] = useState<string>('TODOS')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Lote | null>(null)
  const [removing, setRemoving] = useState<Lote | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['lotes', page, statusFiltro],
    queryFn: () =>
      lotesApi.listar({
        page,
        size: 10,
        status: statusFiltro === 'TODOS' ? undefined : statusFiltro,
      }),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => lotesApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lotes'] })
      toast.success('Lote removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o lote.')),
  })

  function statusVariant(status: string) {
    if (status === 'ATIVO') return 'success' as const
    if (status === 'VENDIDO') return 'secondary' as const
    return 'outline' as const
  }

  return (
    <div>
      <PageHeader
        title="Lotes"
        description="Lotes de produção associados aos tanques da sua fazenda."
        actions={
          podeGerenciar && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Novo lote
            </Button>
          )
        }
      />

      <div className="mb-4 w-48">
        <Select
          value={statusFiltro}
          onValueChange={(v) => { setStatusFiltro(v); setPage(0) }}
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos</SelectItem>
            {Object.entries(statusLoteLabels).map(([value, label]) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : !data?.content.length ? (
        <EmptyState
          icon={Layers}
          title="Nenhum lote cadastrado"
          description="Cadastre um lote para começar a acompanhar alimentação, crescimento e mortalidade."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Novo lote
              </Button>
            )
          }
        />
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Tanque</TableHead>
                <TableHead>Espécie</TableHead>
                <TableHead>Quantidade atual</TableHead>
                <TableHead>Peso atual (g)</TableHead>
                <TableHead>Biomassa (kg)</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((lote) => (
                <TableRow key={lote.id}>
                  <TableCell className="font-medium">{lote.tanqueNome}</TableCell>
                  <TableCell className="text-muted-foreground">{lote.especieNome}</TableCell>
                  <TableCell>{formatNumber(lote.quantidadeAtual)}</TableCell>
                  <TableCell>{formatNumber(lote.pesoAtualG, 1)}</TableCell>
                  <TableCell>{formatNumber(lote.biomassaAtualKg, 1)}</TableCell>
                  <TableCell>
                    <Badge variant={statusVariant(lote.status)}>{statusLoteLabels[lote.status]}</Badge>
                  </TableCell>
                  <TableCell>
                    {podeGerenciar && (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => { setEditing(lote); setFormOpen(true) }}>
                            <Pencil className="mr-2 h-4 w-4" /> Editar
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setRemoving(lote)}
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

      <LoteFormDialog open={formOpen} onOpenChange={setFormOpen} lote={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title={`Remover lote do tanque "${removing?.tanqueNome}"?`}
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

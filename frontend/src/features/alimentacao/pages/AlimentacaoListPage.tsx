import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Utensils, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { ImportExportButtons } from '@/components/shared/ImportExportButtons'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { alimentacaoApi, type Alimentacao } from '../api/alimentacao-api'
import { AlimentacaoFormDialog } from '../components/AlimentacaoFormDialog'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { extractErrorMessage } from '@/lib/api-client'
import { formatCurrency, formatDateTime, formatNumber } from '@/lib/utils'
import { useAuth } from '@/hooks/use-auth'

export default function AlimentacaoListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Alimentacao | null>(null)
  const [removing, setRemoving] = useState<Alimentacao | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['alimentacao', page],
    queryFn: () => alimentacaoApi.listar({ page, size: 10 }),
  })

  const { data: lotes } = useQuery({
    queryKey: ['lotes', 'select', 'ativos'],
    queryFn: () => lotesApi.listar({ size: 100 }),
  })

  const loteLabelPorId = new Map(
    (lotes?.content ?? []).map((lote) => [lote.id, `${lote.tanqueNome} / ${lote.especieNome}`]),
  )

  const deleteMutation = useMutation({
    mutationFn: (id: string) => alimentacaoApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alimentacao'] })
      toast.success('Registro de alimentação removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o registro.')),
  })

  return (
    <div>
      <PageHeader
        title="Alimentação"
        description="Histórico de arraçoamento dos lotes."
        actions={
          <>
            <ImportExportButtons
              entidadeLabel="Registros de alimentação"
              onDownloadTemplate={alimentacaoApi.baixarModelo}
              onExport={alimentacaoApi.exportar}
              onImport={alimentacaoApi.importar}
              onImportComplete={() => queryClient.invalidateQueries({ queryKey: ['alimentacao'] })}
            />
            {podeGerenciar && (
              <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
                <Plus className="h-4 w-4" /> Nova alimentação
              </Button>
            )}
          </>
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
          icon={Utensils}
          title="Nenhum registro de alimentação"
          description="Registre o arraçoamento dos lotes para acompanhar consumo e custos."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Nova alimentação
              </Button>
            )
          }
        />
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Data/hora</TableHead>
                <TableHead>Lote</TableHead>
                <TableHead>Tipo de ração</TableHead>
                <TableHead>Quantidade (kg)</TableHead>
                <TableHead>Custo</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((registro) => (
                <TableRow key={registro.id}>
                  <TableCell className="text-muted-foreground">{formatDateTime(registro.horario)}</TableCell>
                  <TableCell className="font-medium">
                    {loteLabelPorId.get(registro.loteId) ?? '—'}
                  </TableCell>
                  <TableCell>{registro.tipoRacao}</TableCell>
                  <TableCell>{formatNumber(registro.quantidadeKg, 2)}</TableCell>
                  <TableCell className="text-muted-foreground">{formatCurrency(registro.custo)}</TableCell>
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

      <AlimentacaoFormDialog open={formOpen} onOpenChange={setFormOpen} registro={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title="Remover registro de alimentação?"
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Droplets, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
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
import { qualidadeAguaApi, type RegistroQualidadeAgua } from '../api/qualidade-agua-api'
import { QualidadeAguaFormDialog } from '../components/QualidadeAguaFormDialog'
import { tanquesApi } from '@/features/tanques/api/tanques-api'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'
import { formatDateTime, formatNumber } from '@/lib/utils'

export default function QualidadeAguaListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<RegistroQualidadeAgua | null>(null)
  const [removing, setRemoving] = useState<RegistroQualidadeAgua | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['qualidade-agua', page],
    queryFn: () => qualidadeAguaApi.listar({ page, size: 10, sort: 'medidoEm,desc' }),
  })

  const { data: tanques } = useQuery({
    queryKey: ['tanques', 'select'],
    queryFn: () => tanquesApi.listar({ size: 100 }),
  })

  const tanqueNomeMap = new Map((tanques?.content ?? []).map((t) => [t.id, t.nome]))

  const deleteMutation = useMutation({
    mutationFn: (id: string) => qualidadeAguaApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['qualidade-agua'] })
      toast.success('Registro removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o registro.')),
  })

  return (
    <div>
      <PageHeader
        title="Qualidade da água"
        description="Histórico de medições de parâmetros da água dos tanques."
        actions={
          <>
            <ImportExportButtons
              entidadeLabel="Registros de qualidade da água"
              onDownloadTemplate={qualidadeAguaApi.baixarModelo}
              onExport={qualidadeAguaApi.exportar}
              onImport={qualidadeAguaApi.importar}
              onImportComplete={() => queryClient.invalidateQueries({ queryKey: ['qualidade-agua'] })}
            />
            {podeGerenciar && (
              <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
                <Plus className="h-4 w-4" /> Novo registro
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
          icon={Droplets}
          title="Nenhum registro de qualidade da água"
          description="Registre as medições dos parâmetros da água para acompanhar a saúde dos seus tanques."
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
                <TableHead>Data/hora</TableHead>
                <TableHead>Tanque</TableHead>
                <TableHead>Temperatura</TableHead>
                <TableHead>pH</TableHead>
                <TableHead>Oxigênio</TableHead>
                <TableHead>Amônia</TableHead>
                <TableHead>Nitrito</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((registro) => (
                <TableRow key={registro.id}>
                  <TableCell className="font-medium">{formatDateTime(registro.medidoEm)}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {tanqueNomeMap.get(registro.tanqueId) ?? '—'}
                  </TableCell>
                  <TableCell>{formatNumber(registro.temperatura, 2)}</TableCell>
                  <TableCell>{formatNumber(registro.ph, 2)}</TableCell>
                  <TableCell>{formatNumber(registro.oxigenioDissolvido, 2)}</TableCell>
                  <TableCell>{formatNumber(registro.amonia, 2)}</TableCell>
                  <TableCell>{formatNumber(registro.nitrito, 2)}</TableCell>
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

      <QualidadeAguaFormDialog open={formOpen} onOpenChange={setFormOpen} registro={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title="Remover registro de qualidade da água?"
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

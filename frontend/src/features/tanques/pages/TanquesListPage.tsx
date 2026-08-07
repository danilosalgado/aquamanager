import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Waves, MoreHorizontal, Pencil, Trash2, Eye } from 'lucide-react'
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
import { tanquesApi, type Tanque } from '../api/tanques-api'
import { tipoTanqueLabels, statusTanqueLabels } from '../schemas/tanque-schema'
import { TanqueFormDialog } from '../components/TanqueFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'

export default function TanquesListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE')
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Tanque | null>(null)
  const [removing, setRemoving] = useState<Tanque | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['tanques', page],
    queryFn: () => tanquesApi.listar({ page, size: 10 }),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => tanquesApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tanques'] })
      toast.success('Tanque removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o tanque.')),
  })

  function statusVariant(status: string) {
    if (status === 'ATIVO') return 'success' as const
    if (status === 'MANUTENCAO') return 'warning' as const
    return 'secondary' as const
  }

  return (
    <div>
      <PageHeader
        title="Tanques"
        description="Cadastro e status dos tanques da sua fazenda."
        actions={
          podeGerenciar && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Novo tanque
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
          icon={Waves}
          title="Nenhum tanque cadastrado"
          description="Cadastre seu primeiro tanque para começar a registrar lotes e produção."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Novo tanque
              </Button>
            )
          }
        />
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Código</TableHead>
                <TableHead>Tipo</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Volume</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((tanque) => (
                <TableRow key={tanque.id}>
                  <TableCell className="font-medium">
                    <Link to={`/tanques/${tanque.id}`} className="hover:text-primary hover:underline">
                      {tanque.nome}
                    </Link>
                  </TableCell>
                  <TableCell className="text-muted-foreground">{tanque.codigo}</TableCell>
                  <TableCell>{tipoTanqueLabels[tanque.tipo]}</TableCell>
                  <TableCell>
                    <Badge variant={statusVariant(tanque.status)}>{statusTanqueLabels[tanque.status]}</Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {tanque.volumeM3 ? `${tanque.volumeM3} m³` : '—'}
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem asChild>
                          <Link to={`/tanques/${tanque.id}`}>
                            <Eye className="mr-2 h-4 w-4" /> Ver detalhes
                          </Link>
                        </DropdownMenuItem>
                        {podeGerenciar && (
                          <>
                            <DropdownMenuItem onClick={() => { setEditing(tanque); setFormOpen(true) }}>
                              <Pencil className="mr-2 h-4 w-4" /> Editar
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              className="text-destructive focus:text-destructive"
                              onClick={() => setRemoving(tanque)}
                            >
                              <Trash2 className="mr-2 h-4 w-4" /> Remover
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
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

      <TanqueFormDialog open={formOpen} onOpenChange={setFormOpen} tanque={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title={`Remover tanque "${removing?.nome}"?`}
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

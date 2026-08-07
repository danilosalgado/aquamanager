import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Users, MoreHorizontal, Pencil, Trash2, Search } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { clientesApi, type Cliente } from '../api/clientes-api'
import { ClienteFormDialog } from '../components/ClienteFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'

export default function ClientesListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE')
  const [page, setPage] = useState(0)
  const [busca, setBusca] = useState('')
  const [buscaAplicada, setBuscaAplicada] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Cliente | null>(null)
  const [removing, setRemoving] = useState<Cliente | null>(null)
  const queryClient = useQueryClient()

  useEffect(() => {
    const timeout = setTimeout(() => {
      setBuscaAplicada(busca)
      setPage(0)
    }, 400)
    return () => clearTimeout(timeout)
  }, [busca])

  const { data, isLoading } = useQuery({
    queryKey: ['clientes', page, buscaAplicada],
    queryFn: () => clientesApi.listar({ page, size: 10, busca: buscaAplicada || undefined }),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => clientesApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clientes'] })
      toast.success('Cliente removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o cliente.')),
  })

  return (
    <div>
      <PageHeader
        title="Clientes"
        description="Cadastro de clientes da sua fazenda."
        actions={
          podeGerenciar && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Novo cliente
            </Button>
          )
        }
      />

      <div className="mb-4 max-w-sm">
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Buscar por nome..."
            className="pl-9"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : !data?.content.length ? (
        <EmptyState
          icon={Users}
          title="Nenhum cliente encontrado"
          description="Cadastre seu primeiro cliente para começar a registrar vendas."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Novo cliente
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
                <TableHead>Documento</TableHead>
                <TableHead>Telefone</TableHead>
                <TableHead>Email</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((cliente) => (
                <TableRow key={cliente.id}>
                  <TableCell className="font-medium">{cliente.nome}</TableCell>
                  <TableCell className="text-muted-foreground">{cliente.documento ?? '—'}</TableCell>
                  <TableCell className="text-muted-foreground">{cliente.telefone ?? '—'}</TableCell>
                  <TableCell className="text-muted-foreground">{cliente.email ?? '—'}</TableCell>
                  <TableCell>
                    {podeGerenciar && (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => { setEditing(cliente); setFormOpen(true) }}>
                            <Pencil className="mr-2 h-4 w-4" /> Editar
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setRemoving(cliente)}
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

      <ClienteFormDialog open={formOpen} onOpenChange={setFormOpen} cliente={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title={`Remover cliente "${removing?.nome}"?`}
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

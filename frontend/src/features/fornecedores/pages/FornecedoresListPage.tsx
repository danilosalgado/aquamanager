import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Truck, MoreHorizontal, Pencil, Trash2, Search } from 'lucide-react'
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
import { fornecedoresApi, type Fornecedor } from '../api/fornecedores-api'
import { FornecedorFormDialog } from '../components/FornecedorFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'

export default function FornecedoresListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE')
  const [page, setPage] = useState(0)
  const [busca, setBusca] = useState('')
  const [buscaAplicada, setBuscaAplicada] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Fornecedor | null>(null)
  const [removing, setRemoving] = useState<Fornecedor | null>(null)
  const queryClient = useQueryClient()

  useEffect(() => {
    const timeout = setTimeout(() => {
      setBuscaAplicada(busca)
      setPage(0)
    }, 400)
    return () => clearTimeout(timeout)
  }, [busca])

  const { data, isLoading } = useQuery({
    queryKey: ['fornecedores', page, buscaAplicada],
    queryFn: () => fornecedoresApi.listar({ page, size: 10, busca: buscaAplicada || undefined }),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => fornecedoresApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fornecedores'] })
      toast.success('Fornecedor removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o fornecedor.')),
  })

  return (
    <div>
      <PageHeader
        title="Fornecedores"
        description="Cadastro de fornecedores da sua fazenda."
        actions={
          podeGerenciar && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Novo fornecedor
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
          icon={Truck}
          title="Nenhum fornecedor encontrado"
          description="Cadastre seu primeiro fornecedor para começar a registrar compras."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Novo fornecedor
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
              {data.content.map((fornecedor) => (
                <TableRow key={fornecedor.id}>
                  <TableCell className="font-medium">{fornecedor.nome}</TableCell>
                  <TableCell className="text-muted-foreground">{fornecedor.documento ?? '—'}</TableCell>
                  <TableCell className="text-muted-foreground">{fornecedor.telefone ?? '—'}</TableCell>
                  <TableCell className="text-muted-foreground">{fornecedor.email ?? '—'}</TableCell>
                  <TableCell>
                    {podeGerenciar && (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => { setEditing(fornecedor); setFormOpen(true) }}>
                            <Pencil className="mr-2 h-4 w-4" /> Editar
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setRemoving(fornecedor)}
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

      <FornecedorFormDialog open={formOpen} onOpenChange={setFormOpen} fornecedor={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title={`Remover fornecedor "${removing?.nome}"?`}
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

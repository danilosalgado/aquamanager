import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ShieldCheck, UserRound, MoreHorizontal, Pencil, UserX } from 'lucide-react'
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
import { adminUsuariosApi, type UsuarioAdmin } from '../api/admin-usuarios-api'
import { AdminUsuarioEditDialog } from '../components/AdminUsuarioEditDialog'
import { roleLabels } from '@/features/usuarios/schemas/usuario-schema'
import { extractErrorMessage } from '@/lib/api-client'

function roleVariant(role: string) {
  if (role === 'ADMINISTRADOR') return 'default' as const
  if (role === 'GERENTE') return 'success' as const
  if (role === 'FUNCIONARIO') return 'secondary' as const
  return 'outline' as const
}

export default function AdminUsuariosListPage() {
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState<UsuarioAdmin | null>(null)
  const [desativando, setDesativando] = useState<UsuarioAdmin | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'usuarios', page],
    queryFn: () => adminUsuariosApi.listar({ page, size: 15 }),
  })

  const desativarMutation = useMutation({
    mutationFn: (id: string) => adminUsuariosApi.desativar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'usuarios'] })
      toast.success('Usuário desativado.')
      setDesativando(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível desativar o usuário.')),
  })

  return (
    <div>
      <PageHeader
        title="Usuários"
        description="Todos os usuários de todas as empresas clientes da plataforma."
      />

      <div className="mb-4 flex items-center gap-2 rounded-lg border border-primary/30 bg-primary/5 px-3 py-2 text-xs text-muted-foreground">
        <ShieldCheck className="h-4 w-4 shrink-0 text-primary" />
        Você está vendo dados de todos os tenants porque esta é uma conta administrativa da plataforma.
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : !data?.content.length ? (
        <EmptyState icon={UserRound} title="Nenhum usuário encontrado" description="Ainda não há usuários cadastrados em nenhuma empresa." />
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Empresa</TableHead>
                <TableHead>Papel</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((usuario) => (
                <TableRow key={usuario.id}>
                  <TableCell className="font-medium">{usuario.nome}</TableCell>
                  <TableCell className="text-muted-foreground">{usuario.email}</TableCell>
                  <TableCell className="text-muted-foreground">{usuario.empresaNome}</TableCell>
                  <TableCell>
                    <Badge variant={roleVariant(usuario.role)}>{roleLabels[usuario.role]}</Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant={usuario.ativo ? 'success' : 'secondary'}>
                      {usuario.ativo ? 'Ativo' : 'Inativo'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => setEditing(usuario)}>
                          <Pencil className="mr-2 h-4 w-4" /> Editar
                        </DropdownMenuItem>
                        {usuario.ativo && (
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setDesativando(usuario)}
                          >
                            <UserX className="mr-2 h-4 w-4" /> Desativar
                          </DropdownMenuItem>
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

      <AdminUsuarioEditDialog open={!!editing} onOpenChange={(open) => !open && setEditing(null)} usuario={editing} />

      <ConfirmDialog
        open={!!desativando}
        onOpenChange={(open) => !open && setDesativando(null)}
        title={`Desativar usuário "${desativando?.nome}"?`}
        description={`O usuário perderá o acesso à conta "${desativando?.empresaNome}" até ser reativado.`}
        confirmLabel="Desativar"
        loading={desativarMutation.isPending}
        onConfirm={() => desativando && desativarMutation.mutate(desativando.id)}
      />
    </div>
  )
}

import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Plus, UserRound, MoreHorizontal, Pencil, UserX, Search, CheckCircle2, XCircle,
} from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { usuariosApi } from '../api/usuarios-api'
import { roleLabels } from '../schemas/usuario-schema'
import { UsuarioFormDialog } from '../components/UsuarioFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'
import type { Usuario } from '@/types/auth'

function roleVariant(role: string) {
  if (role === 'ADMINISTRADOR') return 'default' as const
  if (role === 'GERENTE') return 'success' as const
  if (role === 'FUNCIONARIO') return 'secondary' as const
  return 'outline' as const
}

export default function UsuariosListPage() {
  const { hasRole } = useAuth()
  const podeEscrever = hasRole('ADMINISTRADOR')
  const [page, setPage] = useState(0)
  const [busca, setBusca] = useState('')
  const [buscaAplicada, setBuscaAplicada] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Usuario | null>(null)
  const [desativando, setDesativando] = useState<Usuario | null>(null)
  const queryClient = useQueryClient()

  useEffect(() => {
    const timeout = setTimeout(() => {
      setBuscaAplicada(busca)
      setPage(0)
    }, 400)
    return () => clearTimeout(timeout)
  }, [busca])

  const { data, isLoading } = useQuery({
    queryKey: ['usuarios', page, buscaAplicada],
    queryFn: () => usuariosApi.listar({ page, size: 10, busca: buscaAplicada || undefined }),
  })

  const desativarMutation = useMutation({
    mutationFn: (id: string) => usuariosApi.desativar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
      toast.success('Usuário desativado.')
      setDesativando(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível desativar o usuário.')),
  })

  return (
    <div>
      <PageHeader
        title="Usuários"
        description="Gestão dos usuários com acesso à sua empresa."
        actions={
          podeEscrever && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Novo usuário
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
          icon={UserRound}
          title="Nenhum usuário encontrado"
          description="Cadastre usuários para dar acesso à sua equipe."
          action={
            podeEscrever && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Novo usuário
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
                <TableHead>Email</TableHead>
                <TableHead>Papel</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Confirmado</TableHead>
                {podeEscrever && <TableHead className="w-10" />}
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((usuario) => (
                <TableRow key={usuario.id}>
                  <TableCell className="font-medium">{usuario.nome}</TableCell>
                  <TableCell className="text-muted-foreground">{usuario.email}</TableCell>
                  <TableCell>
                    <Badge variant={roleVariant(usuario.role)}>{roleLabels[usuario.role]}</Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant={usuario.ativo ? 'success' : 'secondary'}>
                      {usuario.ativo ? 'Ativo' : 'Inativo'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {usuario.emailConfirmado ? (
                      <CheckCircle2 className="h-4 w-4 text-success" />
                    ) : (
                      <XCircle className="h-4 w-4 text-muted-foreground" />
                    )}
                  </TableCell>
                  {podeEscrever && (
                    <TableCell>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => { setEditing(usuario); setFormOpen(true) }}>
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
                  )}
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

      <UsuarioFormDialog open={formOpen} onOpenChange={setFormOpen} usuario={editing} />

      <ConfirmDialog
        open={!!desativando}
        onOpenChange={(open) => !open && setDesativando(null)}
        title={`Desativar usuário "${desativando?.nome}"?`}
        description="O usuário perderá o acesso à plataforma até ser reativado."
        confirmLabel="Desativar"
        loading={desativarMutation.isPending}
        onConfirm={() => desativando && desativarMutation.mutate(desativando.id)}
      />
    </div>
  )
}

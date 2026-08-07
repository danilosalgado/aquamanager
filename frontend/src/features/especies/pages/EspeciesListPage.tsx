import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Fish, MoreHorizontal, Pencil, Trash2, Lock } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { especiesApi, type Especie } from '../api/especies-api'
import { EspecieFormDialog } from '../components/EspecieFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { useAuth } from '@/hooks/use-auth'

export default function EspeciesListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Especie | null>(null)
  const [removing, setRemoving] = useState<Especie | null>(null)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['especies'],
    queryFn: () => especiesApi.listar(),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => especiesApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['especies'] })
      toast.success('Espécie removida.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover a espécie.')),
  })

  return (
    <div>
      <PageHeader
        title="Espécies"
        description="Catálogo de espécies cultivadas e suas faixas ideais de água."
        actions={
          podeGerenciar && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Nova espécie
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
      ) : !data?.length ? (
        <EmptyState
          icon={Fish}
          title="Nenhuma espécie cadastrada"
          description="Cadastre espécies para associá-las aos lotes dos seus tanques."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Nova espécie
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
                <TableHead>Nome científico</TableHead>
                <TableHead>Ciclo padrão</TableHead>
                <TableHead>Peso de abate</TableHead>
                <TableHead />
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.map((especie) => (
                <TableRow key={especie.id}>
                  <TableCell className="font-medium">{especie.nome}</TableCell>
                  <TableCell className="text-muted-foreground italic">
                    {especie.nomeCientifico ?? '—'}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {especie.cicloDiasPadrao ? `${especie.cicloDiasPadrao} dias` : '—'}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {especie.pesoAbatePadraoG ? `${especie.pesoAbatePadraoG} g` : '—'}
                  </TableCell>
                  <TableCell>
                    {especie.global && <Badge variant="secondary">Catálogo global</Badge>}
                  </TableCell>
                  <TableCell>
                    {especie.global ? (
                      <div className="flex h-8 w-8 items-center justify-center text-muted-foreground" title="Somente leitura">
                        <Lock className="h-4 w-4" />
                      </div>
                    ) : podeGerenciar ? (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => { setEditing(especie); setFormOpen(true) }}>
                            <Pencil className="mr-2 h-4 w-4" /> Editar
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setRemoving(especie)}
                          >
                            <Trash2 className="mr-2 h-4 w-4" /> Remover
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    ) : null}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <EspecieFormDialog open={formOpen} onOpenChange={setFormOpen} especie={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title={`Remover espécie "${removing?.nome}"?`}
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

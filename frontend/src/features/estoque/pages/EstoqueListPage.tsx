import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Plus, Package, ArrowLeftRight, MoreHorizontal, Pencil, Trash2, AlertTriangle,
} from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { estoqueApi, type EstoqueItem } from '../api/estoque-api'
import { categoriaLabels, tipoMovimentacaoLabels } from '../schemas/estoque-schema'
import { EstoqueItemFormDialog } from '../components/EstoqueItemFormDialog'
import { MovimentacaoFormDialog } from '../components/MovimentacaoFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { formatDate, formatDateTime, formatNumber } from '@/lib/utils'
import { useAuth } from '@/hooks/use-auth'

export default function EstoqueListPage() {
  const { hasRole } = useAuth()
  const podeGerenciarItens = hasRole('ADMINISTRADOR', 'GERENTE')
  const podeMovimentar = hasRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')

  const [tab, setTab] = useState<'itens' | 'movimentacoes'>('itens')
  const [itensPage, setItensPage] = useState(0)
  const [movPage, setMovPage] = useState(0)

  const [itemFormOpen, setItemFormOpen] = useState(false)
  const [movFormOpen, setMovFormOpen] = useState(false)
  const [editing, setEditing] = useState<EstoqueItem | null>(null)
  const [removing, setRemoving] = useState<EstoqueItem | null>(null)

  const queryClient = useQueryClient()

  const { data: itensData, isLoading: itensLoading } = useQuery({
    queryKey: ['estoque-itens', itensPage],
    queryFn: () => estoqueApi.listarItens({ page: itensPage, size: 10 }),
  })

  const { data: movData, isLoading: movLoading } = useQuery({
    queryKey: ['estoque-movimentacoes', movPage],
    queryFn: () => estoqueApi.listarMovimentacoes({ page: movPage, size: 10 }),
    enabled: tab === 'movimentacoes',
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => estoqueApi.removerItem(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['estoque-itens'] })
      toast.success('Item removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o item.')),
  })

  const movimentacoesOrdenadas = movData
    ? [...movData.content].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
      )
    : []

  return (
    <div>
      <PageHeader
        title="Estoque"
        description="Itens de estoque e movimentações de entrada e saída."
        actions={
          <div className="flex items-center gap-2">
            {podeMovimentar && (
              <Button variant="outline" onClick={() => setMovFormOpen(true)}>
                <ArrowLeftRight className="h-4 w-4" /> Registrar movimentação
              </Button>
            )}
            {podeGerenciarItens && (
              <Button onClick={() => { setEditing(null); setItemFormOpen(true) }}>
                <Plus className="h-4 w-4" /> Novo item
              </Button>
            )}
          </div>
        }
      />

      <Tabs value={tab} onValueChange={(v) => setTab(v as 'itens' | 'movimentacoes')}>
        <TabsList>
          <TabsTrigger value="itens">Itens</TabsTrigger>
          <TabsTrigger value="movimentacoes">Movimentações</TabsTrigger>
        </TabsList>

        <TabsContent value="itens">
          {itensLoading ? (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : !itensData?.content.length ? (
            <EmptyState
              icon={Package}
              title="Nenhum item cadastrado"
              description="Cadastre itens de estoque como ração, medicamentos e equipamentos."
              action={
                podeGerenciarItens && (
                  <Button onClick={() => setItemFormOpen(true)}>
                    <Plus className="h-4 w-4" /> Novo item
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
                    <TableHead>Categoria</TableHead>
                    <TableHead>Quantidade atual</TableHead>
                    <TableHead>Quantidade mínima</TableHead>
                    <TableHead>Fornecedor</TableHead>
                    <TableHead>Validade</TableHead>
                    <TableHead className="w-10" />
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {itensData.content.map((item) => {
                    const estoqueBaixo =
                      item.quantidadeMinima != null && item.quantidadeAtual <= item.quantidadeMinima
                    return (
                      <TableRow key={item.id}>
                        <TableCell className="font-medium">{item.nome}</TableCell>
                        <TableCell>
                          <Badge variant="secondary">{categoriaLabels[item.categoria]}</Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-1.5">
                            <span className={estoqueBaixo ? 'font-medium text-destructive' : undefined}>
                              {formatNumber(item.quantidadeAtual, 2)} {item.unidade}
                            </span>
                            {estoqueBaixo && (
                              <Badge variant="warning" className="gap-1">
                                <AlertTriangle className="h-3 w-3" /> Estoque baixo
                              </Badge>
                            )}
                          </div>
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {item.quantidadeMinima != null ? `${formatNumber(item.quantidadeMinima, 2)} ${item.unidade}` : '—'}
                        </TableCell>
                        <TableCell className="text-muted-foreground">{item.fornecedorNome ?? '—'}</TableCell>
                        <TableCell className="text-muted-foreground">{formatDate(item.validade)}</TableCell>
                        <TableCell>
                          {podeGerenciarItens && (
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className="h-8 w-8">
                                  <MoreHorizontal className="h-4 w-4" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="end">
                                <DropdownMenuItem onClick={() => { setEditing(item); setItemFormOpen(true) }}>
                                  <Pencil className="mr-2 h-4 w-4" /> Editar
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                  className="text-destructive focus:text-destructive"
                                  onClick={() => setRemoving(item)}
                                >
                                  <Trash2 className="mr-2 h-4 w-4" /> Remover
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          )}
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
              <DataTablePagination
                page={itensData.page}
                totalPages={itensData.totalPages}
                totalElements={itensData.totalElements}
                onPageChange={setItensPage}
              />
            </div>
          )}
        </TabsContent>

        <TabsContent value="movimentacoes">
          {movLoading ? (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : !movData?.content.length ? (
            <EmptyState
              icon={ArrowLeftRight}
              title="Nenhuma movimentação registrada"
              description="Entradas e saídas de itens do estoque aparecerão aqui."
            />
          ) : (
            <div className="rounded-xl border border-border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Data</TableHead>
                    <TableHead>Item</TableHead>
                    <TableHead>Tipo</TableHead>
                    <TableHead>Quantidade</TableHead>
                    <TableHead>Motivo</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {movimentacoesOrdenadas.map((mov) => (
                    <TableRow key={mov.id}>
                      <TableCell className="text-muted-foreground">{formatDateTime(mov.createdAt)}</TableCell>
                      <TableCell className="font-medium">{mov.itemNome}</TableCell>
                      <TableCell>
                        <Badge variant={mov.tipo === 'ENTRADA' ? 'success' : 'destructive'}>
                          {tipoMovimentacaoLabels[mov.tipo]}
                        </Badge>
                      </TableCell>
                      <TableCell>{formatNumber(mov.quantidade, 2)}</TableCell>
                      <TableCell className="text-muted-foreground">{mov.motivo ?? '—'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              <DataTablePagination
                page={movData.page}
                totalPages={movData.totalPages}
                totalElements={movData.totalElements}
                onPageChange={setMovPage}
              />
            </div>
          )}
        </TabsContent>
      </Tabs>

      <EstoqueItemFormDialog open={itemFormOpen} onOpenChange={setItemFormOpen} item={editing} />

      <MovimentacaoFormDialog
        open={movFormOpen}
        onOpenChange={setMovFormOpen}
        itens={itensData?.content ?? []}
      />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title={`Remover item "${removing?.nome}"?`}
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

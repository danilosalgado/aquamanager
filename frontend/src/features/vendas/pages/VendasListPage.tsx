import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, ShoppingCart, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { vendasApi, type Venda } from '../api/vendas-api'
import { VendaFormDialog } from '../components/VendaFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { formatCurrency, formatNumber, formatDate } from '@/lib/utils'
import { useAuth } from '@/hooks/use-auth'

export default function VendasListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE')
  const queryClient = useQueryClient()

  const [categoriaAtiva, setCategoriaAtiva] = useState<string>('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Venda | null>(null)
  const [removing, setRemoving] = useState<Venda | null>(null)

  const { data: categorias, isLoading: carregandoCategorias } = useQuery({
    queryKey: ['vendas', 'categorias'],
    queryFn: () => vendasApi.categorias(),
  })

  useEffect(() => {
    if (categorias?.length && !categorias.includes(categoriaAtiva)) {
      setCategoriaAtiva(categorias[0])
    }
  }, [categorias, categoriaAtiva])

  const { data: vendas, isLoading: carregandoVendas } = useQuery({
    queryKey: ['vendas', 'lista', categoriaAtiva],
    queryFn: () => vendasApi.listar({ categoriaProduto: categoriaAtiva, size: 50 }),
    enabled: !!categoriaAtiva,
  })

  const { data: relatorio, isLoading: carregandoRelatorio } = useQuery({
    queryKey: ['vendas', 'relatorio'],
    queryFn: () => vendasApi.relatorioLucroBrutoPorTanque(),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => vendasApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vendas'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro-lancamentos'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro-resumo'] })
      toast.success('Venda removida.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover a venda.')),
  })

  const totalQuantidade = vendas?.content.reduce((acc, v) => acc + v.quantidadeKg, 0) ?? 0
  const totalValor = vendas?.content.reduce((acc, v) => acc + v.valorTotal, 0) ?? 0

  const totaisRelatorio = (relatorio ?? []).reduce(
    (acc, r) => ({
      receita: acc.receita + r.receita,
      custoRacao: acc.custoRacao + r.custoRacao,
      custoOperacional: acc.custoOperacional + r.custoOperacional,
      lucroBruto: acc.lucroBruto + r.lucroBruto,
    }),
    { receita: 0, custoRacao: 0, custoOperacional: 0, lucroBruto: 0 },
  )

  return (
    <div>
      <PageHeader
        title="Vendas"
        description="Registro de vendas por categoria de produto."
        actions={
          podeGerenciar && (
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4" /> Lançar venda
            </Button>
          )
        }
      />

      {carregandoCategorias ? (
        <Skeleton className="mb-4 h-10 w-80" />
      ) : categorias && categorias.length > 0 ? (
        <Tabs value={categoriaAtiva} onValueChange={setCategoriaAtiva} className="mb-4">
          <TabsList>
            {categorias.map((c) => (
              <TabsTrigger key={c} value={c}>
                {c}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
      ) : null}

      {!categorias?.length && !carregandoCategorias ? (
        <EmptyState
          icon={ShoppingCart}
          title="Nenhuma venda registrada"
          description="Lance sua primeira venda pra começar a acompanhar o lucro bruto por tanque."
          action={
            podeGerenciar && (
              <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
                <Plus className="h-4 w-4" /> Lançar venda
              </Button>
            )
          }
        />
      ) : carregandoVendas ? (
        <div className="space-y-2">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : !vendas?.content.length ? (
        <EmptyState
          icon={ShoppingCart}
          title="Nenhuma venda registrada"
          description={`Registre as vendas da categoria "${categoriaAtiva}".`}
          action={
            podeGerenciar && (
              <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
                <Plus className="h-4 w-4" /> Lançar venda
              </Button>
            )
          }
        />
      ) : (
        <div className="mb-8 rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Data</TableHead>
                <TableHead>Lote</TableHead>
                <TableHead>Cliente</TableHead>
                <TableHead className="text-right">Quantidade (kg)</TableHead>
                <TableHead className="text-right">Valor total (R$)</TableHead>
                {podeGerenciar && <TableHead className="w-10" />}
              </TableRow>
            </TableHeader>
            <TableBody>
              {vendas.content.map((venda) => (
                <TableRow key={venda.id}>
                  <TableCell>{formatDate(venda.dataVenda)}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {venda.especieNome} · {venda.tanqueNome}
                  </TableCell>
                  <TableCell className="text-muted-foreground">{venda.clienteNome ?? '—'}</TableCell>
                  <TableCell className="text-right">{formatNumber(venda.quantidadeKg, 1)} kg</TableCell>
                  <TableCell className="text-right font-medium">{formatCurrency(venda.valorTotal)}</TableCell>
                  {podeGerenciar && (
                    <TableCell>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => { setEditing(venda); setFormOpen(true) }}>
                            <Pencil className="mr-2 h-4 w-4" /> Editar
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setRemoving(venda)}
                          >
                            <Trash2 className="mr-2 h-4 w-4" /> Remover
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  )}
                </TableRow>
              ))}
              <TableRow className="bg-muted/30 font-semibold">
                <TableCell colSpan={3}>Total</TableCell>
                <TableCell className="text-right">{formatNumber(totalQuantidade, 1)} kg</TableCell>
                <TableCell className="text-right">{formatCurrency(totalValor)}</TableCell>
                {podeGerenciar && <TableCell />}
              </TableRow>
            </TableBody>
          </Table>
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Relatório · Lucro bruto por tanque</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {carregandoRelatorio ? (
            <div className="space-y-2 p-6">
              {Array.from({ length: 2 }).map((_, i) => (
                <Skeleton key={i} className="h-10 w-full" />
              ))}
            </div>
          ) : !relatorio?.length ? (
            <p className="p-6 text-sm text-muted-foreground">Nenhum tanque ativo para calcular o relatório.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tanque</TableHead>
                  <TableHead className="text-right">Receita (R$)</TableHead>
                  <TableHead className="text-right">Custo ração (R$)</TableHead>
                  <TableHead className="text-right">Custo operacional (R$)</TableHead>
                  <TableHead className="text-right">Lucro bruto (R$)</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {relatorio.map((r) => (
                  <TableRow key={r.tanqueId}>
                    <TableCell className="font-medium">{r.tanqueNome}</TableCell>
                    <TableCell className="text-right">{formatCurrency(r.receita)}</TableCell>
                    <TableCell className="text-right text-muted-foreground">{formatCurrency(r.custoRacao)}</TableCell>
                    <TableCell className="text-right text-muted-foreground">{formatCurrency(r.custoOperacional)}</TableCell>
                    <TableCell className={`text-right font-semibold ${r.lucroBruto >= 0 ? 'text-success' : 'text-destructive'}`}>
                      {formatCurrency(r.lucroBruto)}
                    </TableCell>
                  </TableRow>
                ))}
                <TableRow className="bg-muted/30 font-semibold">
                  <TableCell>Total</TableCell>
                  <TableCell className="text-right">{formatCurrency(totaisRelatorio.receita)}</TableCell>
                  <TableCell className="text-right">{formatCurrency(totaisRelatorio.custoRacao)}</TableCell>
                  <TableCell className="text-right">{formatCurrency(totaisRelatorio.custoOperacional)}</TableCell>
                  <TableCell className={`text-right ${totaisRelatorio.lucroBruto >= 0 ? 'text-success' : 'text-destructive'}`}>
                    {formatCurrency(totaisRelatorio.lucroBruto)}
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <VendaFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        venda={editing}
        categoriaInicial={categoriaAtiva || undefined}
      />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title="Remover esta venda?"
        description="A receita gerada no Financeiro pra essa venda também será removida."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

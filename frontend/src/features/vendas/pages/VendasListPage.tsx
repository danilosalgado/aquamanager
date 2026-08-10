import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Plus, ShoppingCart, MoreHorizontal, Pencil, Trash2, DollarSign, Wheat, Wrench, TrendingUp,
} from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { StatCard } from '@/components/shared/StatCard'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
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

  const { data: resumo, isLoading: carregandoResumo } = useQuery({
    queryKey: ['vendas', 'resumo'],
    queryFn: () => vendasApi.resumoLucroBruto(),
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
          description="Lance sua primeira venda pra começar a acompanhar o lucro bruto."
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
                <TableCell colSpan={2}>Total</TableCell>
                <TableCell className="text-right">{formatNumber(totalQuantidade, 1)} kg</TableCell>
                <TableCell className="text-right">{formatCurrency(totalValor)}</TableCell>
                {podeGerenciar && <TableCell />}
              </TableRow>
            </TableBody>
          </Table>
        </div>
      )}

      <div>
        <h2 className="mb-3 text-sm font-semibold text-muted-foreground">Resumo · Lucro bruto</h2>
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          <StatCard
            label="Receita"
            value={formatCurrency(resumo?.receita ?? 0)}
            icon={DollarSign}
            loading={carregandoResumo}
          />
          <StatCard
            label="Custo ração"
            value={formatCurrency(resumo?.custoRacao ?? 0)}
            icon={Wheat}
            loading={carregandoResumo}
          />
          <StatCard
            label="Custo operacional"
            value={formatCurrency(resumo?.custoOperacional ?? 0)}
            icon={Wrench}
            loading={carregandoResumo}
          />
          <StatCard
            label="Lucro bruto"
            value={formatCurrency(resumo?.lucroBruto ?? 0)}
            icon={TrendingUp}
            tone={(resumo?.lucroBruto ?? 0) >= 0 ? 'success' : 'destructive'}
            loading={carregandoResumo}
          />
        </div>
      </div>

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

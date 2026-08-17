import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Plus, Wallet, TrendingUp, TrendingDown, Percent, MoreHorizontal, Pencil, Trash2, CheckCircle2,
} from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { ImportExportButtons } from '@/components/shared/ImportExportButtons'
import { StatCard } from '@/components/shared/StatCard'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { financeiroApi, type Lancamento } from '../api/financeiro-api'
import { tipoLancamentoLabels, statusLancamentoLabels } from '../schemas/financeiro-schema'
import { LancamentoFormDialog } from '../components/LancamentoFormDialog'
import { extractErrorMessage } from '@/lib/api-client'
import { cn, formatCurrency, formatDate, formatNumber } from '@/lib/utils'
import { useAuth } from '@/hooks/use-auth'

const TODOS = 'TODOS'

function pad(n: number) {
  return String(n).padStart(2, '0')
}

function mesAtualRange() {
  const now = new Date()
  const ano = now.getFullYear()
  const mes = now.getMonth()
  const inicio = `${ano}-${pad(mes + 1)}-01`
  const ultimoDia = new Date(ano, mes + 1, 0).getDate()
  const fim = `${ano}-${pad(mes + 1)}-${pad(ultimoDia)}`
  return { inicio, fim }
}

function statusVariant(status: string) {
  if (status === 'PAGO') return 'success' as const
  if (status === 'ATRASADO') return 'destructive' as const
  if (status === 'CANCELADO') return 'outline' as const
  return 'secondary' as const
}

export default function FinanceiroListPage() {
  const { hasRole } = useAuth()
  const podeGerenciar = hasRole('ADMINISTRADOR', 'GERENTE')
  const podeRemover = hasRole('ADMINISTRADOR')

  const [page, setPage] = useState(0)
  const [tipoFiltro, setTipoFiltro] = useState<string>(TODOS)
  const [statusFiltro, setStatusFiltro] = useState<string>(TODOS)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Lancamento | null>(null)
  const [removing, setRemoving] = useState<Lancamento | null>(null)

  const queryClient = useQueryClient()
  const { inicio, fim } = mesAtualRange()

  const { data, isLoading } = useQuery({
    queryKey: ['financeiro-lancamentos', page, tipoFiltro, statusFiltro],
    queryFn: () =>
      financeiroApi.listar({
        page,
        size: 10,
        tipo: tipoFiltro === TODOS ? undefined : tipoFiltro,
        status: statusFiltro === TODOS ? undefined : statusFiltro,
      }),
  })

  const { data: resumo, isLoading: resumoLoading } = useQuery({
    queryKey: ['financeiro-resumo', inicio, fim],
    queryFn: () => financeiroApi.resumo(inicio, fim),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => financeiroApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['financeiro-lancamentos'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro-resumo'] })
      toast.success('Lançamento removido.')
      setRemoving(null)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível remover o lançamento.')),
  })

  const pagarMutation = useMutation({
    mutationFn: (id: string) => financeiroApi.marcarComoPago(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['financeiro-lancamentos'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro-resumo'] })
      toast.success('Lançamento marcado como pago.')
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível marcar o lançamento como pago.')),
  })

  return (
    <div>
      <PageHeader
        title="Financeiro"
        description="Receitas, despesas e resumo financeiro da sua operação."
        actions={
          <>
            <ImportExportButtons
              entidadeLabel="Lançamentos financeiros"
              onDownloadTemplate={financeiroApi.baixarModelo}
              onExport={financeiroApi.exportar}
              onImport={financeiroApi.importar}
              onImportComplete={() => {
                queryClient.invalidateQueries({ queryKey: ['financeiro-lancamentos'] })
                queryClient.invalidateQueries({ queryKey: ['financeiro-resumo'] })
              }}
            />
            {podeGerenciar && (
              <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
                <Plus className="h-4 w-4" /> Novo lançamento
              </Button>
            )}
          </>
        }
      />

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Receita do mês"
          value={formatCurrency(resumo?.receitaTotal)}
          icon={TrendingUp}
          tone="success"
          loading={resumoLoading}
        />
        <StatCard
          label="Despesa do mês"
          value={formatCurrency(resumo?.despesaTotal)}
          icon={TrendingDown}
          tone="destructive"
          loading={resumoLoading}
        />
        <StatCard
          label="Lucro do mês"
          value={formatCurrency(resumo?.lucro)}
          icon={Wallet}
          tone={resumo && resumo.lucro < 0 ? 'destructive' : 'success'}
          loading={resumoLoading}
        />
        <StatCard
          label="Margem"
          value={resumo ? `${formatNumber(resumo.margemPercentual, 1)}%` : '—'}
          icon={Percent}
          tone="default"
          loading={resumoLoading}
        />
      </div>

      <div className="mb-4 flex flex-wrap gap-3">
        <Select value={tipoFiltro} onValueChange={(v) => { setTipoFiltro(v); setPage(0) }}>
          <SelectTrigger className="w-48">
            <SelectValue placeholder="Tipo" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TODOS}>Todos os tipos</SelectItem>
            {Object.entries(tipoLancamentoLabels).map(([value, label]) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select value={statusFiltro} onValueChange={(v) => { setStatusFiltro(v); setPage(0) }}>
          <SelectTrigger className="w-48">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TODOS}>Todos os status</SelectItem>
            {Object.entries(statusLancamentoLabels).map(([value, label]) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : !data?.content.length ? (
        <EmptyState
          icon={Wallet}
          title="Nenhum lançamento encontrado"
          description="Cadastre receitas e despesas para acompanhar o financeiro da sua operação."
          action={
            podeGerenciar && (
              <Button onClick={() => setFormOpen(true)}>
                <Plus className="h-4 w-4" /> Novo lançamento
              </Button>
            )
          }
        />
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Descrição</TableHead>
                <TableHead>Categoria</TableHead>
                <TableHead>Tipo</TableHead>
                <TableHead>Valor</TableHead>
                <TableHead>Vencimento</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.content.map((lancamento) => (
                <TableRow key={lancamento.id}>
                  <TableCell className="font-medium">{lancamento.descricao}</TableCell>
                  <TableCell className="text-muted-foreground">{lancamento.categoria}</TableCell>
                  <TableCell>
                    <Badge variant={lancamento.tipo === 'RECEITA' ? 'success' : 'destructive'}>
                      {tipoLancamentoLabels[lancamento.tipo]}
                    </Badge>
                  </TableCell>
                  <TableCell
                    className={cn(
                      'font-medium',
                      lancamento.tipo === 'RECEITA' ? 'text-success' : 'text-destructive',
                    )}
                  >
                    {formatCurrency(lancamento.valor)}
                  </TableCell>
                  <TableCell className="text-muted-foreground">{formatDate(lancamento.dataVencimento)}</TableCell>
                  <TableCell>
                    <Badge variant={statusVariant(lancamento.status)}>{statusLancamentoLabels[lancamento.status]}</Badge>
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        {podeGerenciar && (
                          <DropdownMenuItem onClick={() => { setEditing(lancamento); setFormOpen(true) }}>
                            <Pencil className="mr-2 h-4 w-4" /> Editar
                          </DropdownMenuItem>
                        )}
                        {podeGerenciar && (lancamento.status === 'PENDENTE' || lancamento.status === 'ATRASADO') && (
                          <DropdownMenuItem onClick={() => pagarMutation.mutate(lancamento.id)}>
                            <CheckCircle2 className="mr-2 h-4 w-4" /> Marcar como pago
                          </DropdownMenuItem>
                        )}
                        {podeRemover && (
                          <DropdownMenuItem
                            className="text-destructive focus:text-destructive"
                            onClick={() => setRemoving(lancamento)}
                          >
                            <Trash2 className="mr-2 h-4 w-4" /> Remover
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

      <LancamentoFormDialog open={formOpen} onOpenChange={setFormOpen} lancamento={editing} />

      <ConfirmDialog
        open={!!removing}
        onOpenChange={(open) => !open && setRemoving(null)}
        title={`Remover lançamento "${removing?.descricao}"?`}
        description="Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        loading={deleteMutation.isPending}
        onConfirm={() => removing && deleteMutation.mutate(removing.id)}
      />
    </div>
  )
}

import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import {
  lancamentoSchema, type LancamentoFormValues, tipoLancamentoLabels, formasPagamento,
} from '../schemas/financeiro-schema'
import { financeiroApi, type Lancamento } from '../api/financeiro-api'
import { clientesApi } from '@/features/clientes/api/clientes-api'
import { fornecedoresApi } from '@/features/fornecedores/api/fornecedores-api'
import { extractErrorMessage } from '@/lib/api-client'

export function LancamentoFormDialog({
  open,
  onOpenChange,
  lancamento,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  lancamento?: Lancamento | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!lancamento

  const { data: clientes } = useQuery({
    queryKey: ['clientes', 'select'],
    queryFn: () => clientesApi.listar({ size: 100 }),
    enabled: open,
  })

  const { data: fornecedores } = useQuery({
    queryKey: ['fornecedores', 'select'],
    queryFn: () => fornecedoresApi.listar({ size: 100 }),
    enabled: open,
  })

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<LancamentoFormValues>({
    resolver: zodResolver(lancamentoSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        lancamento
          ? {
              tipo: lancamento.tipo,
              categoria: lancamento.categoria,
              descricao: lancamento.descricao,
              valor: lancamento.valor,
              dataVencimento: lancamento.dataVencimento,
              formaPagamento: lancamento.formaPagamento ?? undefined,
              clienteId: lancamento.clienteId ?? undefined,
              fornecedorId: lancamento.fornecedorId ?? undefined,
              loteId: lancamento.loteId ?? undefined,
            }
          : {},
      )
    }
  }, [open, lancamento, reset])

  const mutation = useMutation({
    mutationFn: (values: LancamentoFormValues) =>
      isEditing ? financeiroApi.atualizar(lancamento!.id, values) : financeiroApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['financeiro-lancamentos'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro-resumo'] })
      toast.success(isEditing ? 'Lançamento atualizado.' : 'Lançamento criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o lançamento.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar lançamento' : 'Novo lançamento'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label>Tipo</Label>
              <Select
                value={watch('tipo')}
                onValueChange={(v) => setValue('tipo', v as LancamentoFormValues['tipo'])}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(tipoLancamentoLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipo && <p className="text-xs text-destructive">{errors.tipo.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="categoria">Categoria</Label>
              <Input id="categoria" placeholder="Ex: Ração, Venda de peixes..." {...register('categoria')} />
              {errors.categoria && <p className="text-xs text-destructive">{errors.categoria.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="descricao">Descrição</Label>
              <Input id="descricao" {...register('descricao')} />
              {errors.descricao && <p className="text-xs text-destructive">{errors.descricao.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="valor">Valor</Label>
              <Input id="valor" type="number" step="0.01" {...register('valor')} />
              {errors.valor && <p className="text-xs text-destructive">{errors.valor.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="dataVencimento">Data de vencimento</Label>
              <Input id="dataVencimento" type="date" {...register('dataVencimento')} />
              {errors.dataVencimento && (
                <p className="text-xs text-destructive">{errors.dataVencimento.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label>Forma de pagamento</Label>
              <Select
                value={watch('formaPagamento') ?? ''}
                onValueChange={(v) => setValue('formaPagamento', v)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione (opcional)" />
                </SelectTrigger>
                <SelectContent>
                  {formasPagamento.map((forma) => (
                    <SelectItem key={forma} value={forma}>
                      {forma}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-1.5">
              <Label>Cliente</Label>
              <Select value={watch('clienteId') ?? ''} onValueChange={(v) => setValue('clienteId', v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione (opcional)" />
                </SelectTrigger>
                <SelectContent>
                  {clientes?.content.map((cliente) => (
                    <SelectItem key={cliente.id} value={cliente.id}>
                      {cliente.nome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-1.5">
              <Label>Fornecedor</Label>
              <Select value={watch('fornecedorId') ?? ''} onValueChange={(v) => setValue('fornecedorId', v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione (opcional)" />
                </SelectTrigger>
                <SelectContent>
                  {fornecedores?.content.map((fornecedor) => (
                    <SelectItem key={fornecedor.id} value={fornecedor.id}>
                      {fornecedor.nome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              {isEditing ? 'Salvar alterações' : 'Criar lançamento'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

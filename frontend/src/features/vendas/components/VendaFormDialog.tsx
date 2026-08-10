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
import { vendaSchema, type VendaFormValues, categoriasSugeridas } from '../schemas/venda-schema'
import { vendasApi, type Venda } from '../api/vendas-api'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { clientesApi } from '@/features/clientes/api/clientes-api'
import { extractErrorMessage } from '@/lib/api-client'

export function VendaFormDialog({
  open,
  onOpenChange,
  venda,
  categoriaInicial,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  venda?: Venda | null
  categoriaInicial?: string
}) {
  const queryClient = useQueryClient()
  const isEditing = !!venda

  const { data: lotes } = useQuery({
    queryKey: ['lotes', 'select-vendas'],
    queryFn: () => lotesApi.listar({ status: 'ATIVO', size: 100 }),
    enabled: open,
  })

  const { data: clientes } = useQuery({
    queryKey: ['clientes', 'select'],
    queryFn: () => clientesApi.listar({ size: 100 }),
    enabled: open,
  })

  const { data: categorias } = useQuery({
    queryKey: ['vendas', 'categorias'],
    queryFn: () => vendasApi.categorias(),
    enabled: open,
  })

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<VendaFormValues>({ resolver: zodResolver(vendaSchema) })

  useEffect(() => {
    if (open) {
      reset(
        venda
          ? {
              loteId: venda.loteId,
              categoriaProduto: venda.categoriaProduto,
              quantidadeKg: venda.quantidadeKg,
              valorTotal: venda.valorTotal,
              dataVenda: venda.dataVenda,
              clienteId: venda.clienteId ?? undefined,
              observacoes: venda.observacoes ?? undefined,
            }
          : { categoriaProduto: categoriaInicial ?? '' },
      )
    }
  }, [open, venda, categoriaInicial, reset])

  const mutation = useMutation({
    mutationFn: (values: VendaFormValues) =>
      isEditing ? vendasApi.atualizar(venda!.id, values) : vendasApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vendas'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro-lancamentos'] })
      queryClient.invalidateQueries({ queryKey: ['financeiro-resumo'] })
      toast.success(isEditing ? 'Venda atualizada.' : 'Venda lançada.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar a venda.')),
  })

  const opcoesCategorias = Array.from(new Set([...(categorias ?? []), ...categoriasSugeridas]))

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar venda' : 'Lançar venda'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label>Lote</Label>
              <Select value={watch('loteId') ?? ''} onValueChange={(v) => setValue('loteId', v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione o lote" />
                </SelectTrigger>
                <SelectContent>
                  {lotes?.content.map((lote) => (
                    <SelectItem key={lote.id} value={lote.id}>
                      {lote.especieNome} · {lote.tanqueNome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.loteId && <p className="text-xs text-destructive">{errors.loteId.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="categoriaProduto">Categoria do produto</Label>
              <Input
                id="categoriaProduto"
                list="categorias-sugeridas"
                placeholder="Ex: Filé, Peixe Inteiro Bruto..."
                {...register('categoriaProduto')}
              />
              <datalist id="categorias-sugeridas">
                {opcoesCategorias.map((c) => (
                  <option key={c} value={c} />
                ))}
              </datalist>
              {errors.categoriaProduto && (
                <p className="text-xs text-destructive">{errors.categoriaProduto.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="quantidadeKg">Quantidade (kg)</Label>
              <Input id="quantidadeKg" type="number" step="0.001" {...register('quantidadeKg')} />
              {errors.quantidadeKg && <p className="text-xs text-destructive">{errors.quantidadeKg.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="valorTotal">Valor total (R$)</Label>
              <Input id="valorTotal" type="number" step="0.01" {...register('valorTotal')} />
              {errors.valorTotal && <p className="text-xs text-destructive">{errors.valorTotal.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="dataVenda">Data da venda</Label>
              <Input id="dataVenda" type="date" {...register('dataVenda')} />
              {errors.dataVenda && <p className="text-xs text-destructive">{errors.dataVenda.message}</p>}
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

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="observacoes">Observações</Label>
              <Input id="observacoes" {...register('observacoes')} />
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              {isEditing ? 'Salvar alterações' : 'Lançar venda'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

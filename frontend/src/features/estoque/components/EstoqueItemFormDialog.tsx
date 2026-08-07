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
import { itemSchema, type ItemFormValues, categoriaLabels } from '../schemas/estoque-schema'
import { estoqueApi, type EstoqueItem } from '../api/estoque-api'
import { fornecedoresApi } from '@/features/fornecedores/api/fornecedores-api'
import { extractErrorMessage } from '@/lib/api-client'

export function EstoqueItemFormDialog({
  open,
  onOpenChange,
  item,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  item?: EstoqueItem | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!item

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
  } = useForm<ItemFormValues>({
    resolver: zodResolver(itemSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        item
          ? {
              categoria: item.categoria,
              nome: item.nome,
              unidade: item.unidade,
              quantidadeMinima: item.quantidadeMinima ?? undefined,
              fornecedorId: item.fornecedorId ?? undefined,
              validade: item.validade ?? undefined,
              precoUnitario: item.precoUnitario ?? undefined,
            }
          : {},
      )
    }
  }, [open, item, reset])

  const mutation = useMutation({
    mutationFn: (values: ItemFormValues) =>
      isEditing ? estoqueApi.atualizarItem(item!.id, values) : estoqueApi.criarItem(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['estoque-itens'] })
      toast.success(isEditing ? 'Item atualizado.' : 'Item criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o item.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar item' : 'Novo item'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nome">Nome</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label>Categoria</Label>
              <Select
                value={watch('categoria')}
                onValueChange={(v) => setValue('categoria', v as ItemFormValues['categoria'])}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(categoriaLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.categoria && <p className="text-xs text-destructive">{errors.categoria.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="unidade">Unidade</Label>
              <Input id="unidade" placeholder="kg, un, L..." {...register('unidade')} />
              {errors.unidade && <p className="text-xs text-destructive">{errors.unidade.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="quantidadeMinima">Quantidade mínima</Label>
              <Input id="quantidadeMinima" type="number" step="0.01" {...register('quantidadeMinima')} />
              {errors.quantidadeMinima && (
                <p className="text-xs text-destructive">{errors.quantidadeMinima.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="precoUnitario">Preço unitário</Label>
              <Input id="precoUnitario" type="number" step="0.01" {...register('precoUnitario')} />
              {errors.precoUnitario && (
                <p className="text-xs text-destructive">{errors.precoUnitario.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="validade">Validade</Label>
              <Input id="validade" type="date" {...register('validade')} />
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label>Fornecedor</Label>
              <Select
                value={watch('fornecedorId') ?? ''}
                onValueChange={(v) => setValue('fornecedorId', v)}
              >
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
              {errors.fornecedorId && (
                <p className="text-xs text-destructive">{errors.fornecedorId.message}</p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              {isEditing ? 'Salvar alterações' : 'Criar item'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

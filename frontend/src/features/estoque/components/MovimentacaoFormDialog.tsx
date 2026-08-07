import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
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
import { movimentacaoSchema, type MovimentacaoFormValues, tipoMovimentacaoLabels } from '../schemas/estoque-schema'
import { estoqueApi, type EstoqueItem } from '../api/estoque-api'
import { extractErrorMessage } from '@/lib/api-client'

export function MovimentacaoFormDialog({
  open,
  onOpenChange,
  itens,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  itens: EstoqueItem[]
}) {
  const queryClient = useQueryClient()

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<MovimentacaoFormValues>({
    resolver: zodResolver(movimentacaoSchema),
  })

  useEffect(() => {
    if (open) {
      reset({})
    }
  }, [open, reset])

  const mutation = useMutation({
    mutationFn: (values: MovimentacaoFormValues) => estoqueApi.registrarMovimentacao(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['estoque-itens'] })
      queryClient.invalidateQueries({ queryKey: ['estoque-movimentacoes'] })
      toast.success('Movimentação registrada.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível registrar a movimentação.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Registrar movimentação</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="space-y-1.5">
            <Label>Item</Label>
            <Select value={watch('itemId')} onValueChange={(v) => setValue('itemId', v)}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione o item" />
              </SelectTrigger>
              <SelectContent>
                {itens.map((item) => (
                  <SelectItem key={item.id} value={item.id}>
                    {item.nome} ({item.unidade})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.itemId && <p className="text-xs text-destructive">{errors.itemId.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label>Tipo</Label>
              <Select
                value={watch('tipo')}
                onValueChange={(v) => setValue('tipo', v as MovimentacaoFormValues['tipo'])}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(tipoMovimentacaoLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipo && <p className="text-xs text-destructive">{errors.tipo.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="quantidade">Quantidade</Label>
              <Input id="quantidade" type="number" step="0.01" {...register('quantidade')} />
              {errors.quantidade && <p className="text-xs text-destructive">{errors.quantidade.message}</p>}
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="motivo">Motivo</Label>
            <Input id="motivo" placeholder="Opcional" {...register('motivo')} />
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              Registrar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

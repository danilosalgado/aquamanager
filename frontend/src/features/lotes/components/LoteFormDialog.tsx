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
import { loteSchema, type LoteFormValues, statusLoteLabels } from '../schemas/lote-schema'
import { lotesApi, type Lote } from '../api/lotes-api'
import { tanquesApi } from '@/features/tanques/api/tanques-api'
import { especiesApi } from '@/features/especies/api/especies-api'
import { extractErrorMessage } from '@/lib/api-client'

export function LoteFormDialog({
  open,
  onOpenChange,
  lote,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  lote?: Lote | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!lote

  const { data: tanques } = useQuery({
    queryKey: ['tanques', 'select'],
    queryFn: () => tanquesApi.listar({ size: 100 }),
    enabled: open,
  })

  const { data: especies } = useQuery({
    queryKey: ['especies', 'select'],
    queryFn: () => especiesApi.listar(),
    enabled: open,
  })

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<LoteFormValues>({
    resolver: zodResolver(loteSchema),
    defaultValues: { status: 'ATIVO' },
  })

  useEffect(() => {
    if (open) {
      reset(
        lote
          ? {
              tanqueId: lote.tanqueId,
              especieId: lote.especieId,
              fornecedor: lote.fornecedor ?? undefined,
              quantidadeInicial: lote.quantidadeInicial,
              pesoInicialG: lote.pesoInicialG,
              valorCompra: lote.valorCompra ?? undefined,
              dataCompra: lote.dataCompra,
              previsaoVenda: lote.previsaoVenda ?? undefined,
              status: lote.status,
            }
          : { status: 'ATIVO' },
      )
    }
  }, [open, lote, reset])

  const mutation = useMutation({
    mutationFn: (values: LoteFormValues) =>
      isEditing ? lotesApi.atualizar(lote!.id, values) : lotesApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lotes'] })
      toast.success(isEditing ? 'Lote atualizado.' : 'Lote criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o lote.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar lote' : 'Novo lote'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label>Tanque</Label>
              <Select
                value={watch('tanqueId')}
                onValueChange={(v) => setValue('tanqueId', v)}
                disabled={isEditing}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {tanques?.content.map((tanque) => (
                    <SelectItem key={tanque.id} value={tanque.id}>
                      {tanque.nome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tanqueId && <p className="text-xs text-destructive">{errors.tanqueId.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label>Espécie</Label>
              <Select
                value={watch('especieId')}
                onValueChange={(v) => setValue('especieId', v)}
                disabled={isEditing}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {especies?.map((especie) => (
                    <SelectItem key={especie.id} value={especie.id}>
                      {especie.nome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.especieId && <p className="text-xs text-destructive">{errors.especieId.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="fornecedor">Fornecedor</Label>
              <Input id="fornecedor" {...register('fornecedor')} />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="quantidadeInicial">Quantidade inicial</Label>
              <Input
                id="quantidadeInicial"
                type="number"
                step="1"
                disabled={isEditing}
                {...register('quantidadeInicial')}
              />
              {errors.quantidadeInicial && (
                <p className="text-xs text-destructive">{errors.quantidadeInicial.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="pesoInicialG">Peso inicial (g)</Label>
              <Input
                id="pesoInicialG"
                type="number"
                step="0.01"
                disabled={isEditing}
                {...register('pesoInicialG')}
              />
              {errors.pesoInicialG && <p className="text-xs text-destructive">{errors.pesoInicialG.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="valorCompra">Valor da compra</Label>
              <Input id="valorCompra" type="number" step="0.01" {...register('valorCompra')} />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="dataCompra">Data da compra</Label>
              <Input id="dataCompra" type="date" {...register('dataCompra')} />
              {errors.dataCompra && <p className="text-xs text-destructive">{errors.dataCompra.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="previsaoVenda">Previsão de venda</Label>
              <Input id="previsaoVenda" type="date" {...register('previsaoVenda')} />
            </div>

            <div className="space-y-1.5">
              <Label>Status</Label>
              <Select value={watch('status')} onValueChange={(v) => setValue('status', v as LoteFormValues['status'])}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(statusLoteLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
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
              {isEditing ? 'Salvar alterações' : 'Criar lote'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

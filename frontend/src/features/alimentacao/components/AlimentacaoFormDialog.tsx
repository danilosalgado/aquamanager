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
import { alimentacaoSchema, type AlimentacaoFormValues } from '../schemas/alimentacao-schema'
import { alimentacaoApi, type Alimentacao, type AlimentacaoPayload } from '../api/alimentacao-api'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { extractErrorMessage } from '@/lib/api-client'

/** Converte um instante ISO para o formato aceito por <input type="datetime-local"> (horário local, sem timezone). */
function toDatetimeLocalValue(iso: string): string {
  const date = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function AlimentacaoFormDialog({
  open,
  onOpenChange,
  registro,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  registro?: Alimentacao | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!registro

  const { data: lotes } = useQuery({
    queryKey: ['lotes', 'select', 'ativos'],
    queryFn: () => lotesApi.listar({ status: 'ATIVO', size: 100 }),
    enabled: open,
  })

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<AlimentacaoFormValues>({
    resolver: zodResolver(alimentacaoSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        registro
          ? {
              loteId: registro.loteId,
              tipoRacao: registro.tipoRacao,
              fornecedor: registro.fornecedor ?? undefined,
              quantidadeKg: registro.quantidadeKg,
              horario: toDatetimeLocalValue(registro.horario),
              custo: registro.custo ?? undefined,
            }
          : {},
      )
    }
  }, [open, registro, reset])

  const mutation = useMutation({
    mutationFn: (values: AlimentacaoFormValues) => {
      const payload: AlimentacaoPayload = {
        ...values,
        horario: new Date(values.horario).toISOString(),
      }
      return isEditing ? alimentacaoApi.atualizar(registro!.id, payload) : alimentacaoApi.criar(payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alimentacao'] })
      toast.success(isEditing ? 'Registro de alimentação atualizado.' : 'Alimentação registrada.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o registro.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar alimentação' : 'Nova alimentação'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label>Lote</Label>
              <Select value={watch('loteId')} onValueChange={(v) => setValue('loteId', v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {lotes?.content.map((lote) => (
                    <SelectItem key={lote.id} value={lote.id}>
                      {lote.tanqueNome} — {lote.especieNome}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.loteId && <p className="text-xs text-destructive">{errors.loteId.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="tipoRacao">Tipo de ração</Label>
              <Input id="tipoRacao" {...register('tipoRacao')} />
              {errors.tipoRacao && <p className="text-xs text-destructive">{errors.tipoRacao.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="fornecedor">Fornecedor</Label>
              <Input id="fornecedor" {...register('fornecedor')} />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="quantidadeKg">Quantidade (kg)</Label>
              <Input id="quantidadeKg" type="number" step="0.01" {...register('quantidadeKg')} />
              {errors.quantidadeKg && <p className="text-xs text-destructive">{errors.quantidadeKg.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="horario">Data e hora</Label>
              <Input id="horario" type="datetime-local" {...register('horario')} />
              {errors.horario && <p className="text-xs text-destructive">{errors.horario.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="custo">Custo</Label>
              <Input id="custo" type="number" step="0.01" {...register('custo')} />
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              {isEditing ? 'Salvar alterações' : 'Registrar alimentação'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

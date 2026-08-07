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
import { crescimentoSchema, type CrescimentoFormValues } from '../schemas/crescimento-schema'
import { crescimentoApi, type RegistroCrescimento } from '../api/crescimento-api'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { extractErrorMessage } from '@/lib/api-client'

export function CrescimentoFormDialog({
  open,
  onOpenChange,
  registro,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  registro?: RegistroCrescimento | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!registro

  const { data: lotes } = useQuery({
    queryKey: ['lotes', 'select', 'ativo'],
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
  } = useForm<CrescimentoFormValues>({
    resolver: zodResolver(crescimentoSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        registro
          ? {
              loteId: registro.loteId,
              pesoMedioG: registro.pesoMedioG,
              quantidadeAmostra: registro.quantidadeAmostra,
              dataPesagem: registro.dataPesagem,
            }
          : undefined,
      )
    }
  }, [open, registro, reset])

  const mutation = useMutation({
    mutationFn: (values: CrescimentoFormValues) =>
      isEditing ? crescimentoApi.atualizar(registro!.id, values) : crescimentoApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['crescimento'] })
      toast.success(isEditing ? 'Registro atualizado.' : 'Registro criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o registro.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar registro de crescimento' : 'Novo registro de crescimento'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label>Lote</Label>
              <Select value={watch('loteId')} onValueChange={(v) => setValue('loteId', v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione um lote" />
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
              <Label htmlFor="pesoMedioG">Peso médio (g)</Label>
              <Input id="pesoMedioG" type="number" step="0.01" {...register('pesoMedioG')} />
              {errors.pesoMedioG && <p className="text-xs text-destructive">{errors.pesoMedioG.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="quantidadeAmostra">Quantidade da amostra</Label>
              <Input id="quantidadeAmostra" type="number" step="1" {...register('quantidadeAmostra')} />
              {errors.quantidadeAmostra && (
                <p className="text-xs text-destructive">{errors.quantidadeAmostra.message}</p>
              )}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="dataPesagem">Data da pesagem</Label>
              <Input id="dataPesagem" type="date" {...register('dataPesagem')} />
              {errors.dataPesagem && <p className="text-xs text-destructive">{errors.dataPesagem.message}</p>}
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              {isEditing ? 'Salvar alterações' : 'Registrar'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

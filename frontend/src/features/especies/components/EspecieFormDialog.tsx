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
import { Separator } from '@/components/ui/separator'
import { especieSchema, type EspecieFormValues } from '../schemas/especie-schema'
import { especiesApi, type Especie } from '../api/especies-api'
import { extractErrorMessage } from '@/lib/api-client'

export function EspecieFormDialog({
  open,
  onOpenChange,
  especie,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  especie?: Especie | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!especie

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<EspecieFormValues>({
    resolver: zodResolver(especieSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        especie
          ? {
              nome: especie.nome,
              nomeCientifico: especie.nomeCientifico ?? undefined,
              cicloDiasPadrao: especie.cicloDiasPadrao ?? undefined,
              pesoAbatePadraoG: especie.pesoAbatePadraoG ?? undefined,
              tempMin: especie.tempMin ?? undefined,
              tempMax: especie.tempMax ?? undefined,
              phMin: especie.phMin ?? undefined,
              phMax: especie.phMax ?? undefined,
              oxigenioMin: especie.oxigenioMin ?? undefined,
              amoniaMax: especie.amoniaMax ?? undefined,
              nitritoMax: especie.nitritoMax ?? undefined,
            }
          : {},
      )
    }
  }, [open, especie, reset])

  const mutation = useMutation({
    mutationFn: (values: EspecieFormValues) =>
      isEditing ? especiesApi.atualizar(especie!.id, values) : especiesApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['especies'] })
      toast.success(isEditing ? 'Espécie atualizada.' : 'Espécie criada.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar a espécie.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar espécie' : 'Nova espécie'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nome">Nome</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nomeCientifico">Nome científico</Label>
              <Input id="nomeCientifico" {...register('nomeCientifico')} />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="cicloDiasPadrao">Ciclo padrão (dias)</Label>
              <Input id="cicloDiasPadrao" type="number" step="1" {...register('cicloDiasPadrao')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pesoAbatePadraoG">Peso de abate padrão (g)</Label>
              <Input id="pesoAbatePadraoG" type="number" step="0.01" {...register('pesoAbatePadraoG')} />
            </div>
          </div>

          <Separator />
          <p className="text-sm font-medium">Faixas ideais de água</p>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label htmlFor="tempMin">Temperatura mín. (°C)</Label>
              <Input id="tempMin" type="number" step="0.1" {...register('tempMin')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="tempMax">Temperatura máx. (°C)</Label>
              <Input id="tempMax" type="number" step="0.1" {...register('tempMax')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="phMin">pH mín.</Label>
              <Input id="phMin" type="number" step="0.1" {...register('phMin')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="phMax">pH máx.</Label>
              <Input id="phMax" type="number" step="0.1" {...register('phMax')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="oxigenioMin">Oxigênio mínimo (mg/L)</Label>
              <Input id="oxigenioMin" type="number" step="0.1" {...register('oxigenioMin')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="amoniaMax">Amônia máxima (mg/L)</Label>
              <Input id="amoniaMax" type="number" step="0.01" {...register('amoniaMax')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="nitritoMax">Nitrito máximo (mg/L)</Label>
              <Input id="nitritoMax" type="number" step="0.01" {...register('nitritoMax')} />
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              {isEditing ? 'Salvar alterações' : 'Criar espécie'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

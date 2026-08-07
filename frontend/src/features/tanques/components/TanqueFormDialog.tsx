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
import { tanqueSchema, type TanqueFormValues, tipoTanqueLabels, statusTanqueLabels } from '../schemas/tanque-schema'
import { tanquesApi, type Tanque } from '../api/tanques-api'
import { extractErrorMessage } from '@/lib/api-client'

export function TanqueFormDialog({
  open,
  onOpenChange,
  tanque,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  tanque?: Tanque | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!tanque

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<TanqueFormValues>({
    resolver: zodResolver(tanqueSchema),
    defaultValues: { status: 'ATIVO' },
  })

  useEffect(() => {
    if (open) {
      reset(
        tanque
          ? {
              nome: tanque.nome,
              codigo: tanque.codigo,
              tipo: tanque.tipo,
              areaM2: tanque.areaM2 ?? undefined,
              volumeM3: tanque.volumeM3 ?? undefined,
              profundidadeM: tanque.profundidadeM ?? undefined,
              capacidadeEstimadaKg: tanque.capacidadeEstimadaKg ?? undefined,
              status: tanque.status,
              observacoes: tanque.observacoes ?? undefined,
            }
          : { status: 'ATIVO' },
      )
    }
  }, [open, tanque, reset])

  const mutation = useMutation({
    mutationFn: (values: TanqueFormValues) =>
      isEditing ? tanquesApi.atualizar(tanque!.id, values) : tanquesApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tanques'] })
      toast.success(isEditing ? 'Tanque atualizado.' : 'Tanque criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o tanque.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar tanque' : 'Novo tanque'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nome">Nome</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="codigo">Código</Label>
              <Input id="codigo" {...register('codigo')} />
              {errors.codigo && <p className="text-xs text-destructive">{errors.codigo.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label>Tipo</Label>
              <Select value={watch('tipo')} onValueChange={(v) => setValue('tipo', v as TanqueFormValues['tipo'])}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(tipoTanqueLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.tipo && <p className="text-xs text-destructive">{errors.tipo.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="areaM2">Área (m²)</Label>
              <Input id="areaM2" type="number" step="0.01" {...register('areaM2')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="volumeM3">Volume (m³)</Label>
              <Input id="volumeM3" type="number" step="0.01" {...register('volumeM3')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="profundidadeM">Profundidade (m)</Label>
              <Input id="profundidadeM" type="number" step="0.01" {...register('profundidadeM')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="capacidadeEstimadaKg">Capacidade estimada (kg)</Label>
              <Input id="capacidadeEstimadaKg" type="number" step="0.01" {...register('capacidadeEstimadaKg')} />
            </div>

            <div className="space-y-1.5">
              <Label>Status</Label>
              <Select value={watch('status')} onValueChange={(v) => setValue('status', v as TanqueFormValues['status'])}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(statusTanqueLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
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
              {isEditing ? 'Salvar alterações' : 'Criar tanque'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

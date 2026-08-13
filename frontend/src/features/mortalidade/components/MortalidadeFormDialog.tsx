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
import { cn } from '@/lib/utils'
import { mortalidadeSchema, type MortalidadeFormValues } from '../schemas/mortalidade-schema'
import { mortalidadeApi, CAUSA_EXCLUSAO_LABELS, type RegistroMortalidade } from '../api/mortalidade-api'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { extractErrorMessage } from '@/lib/api-client'

export function MortalidadeFormDialog({
  open,
  onOpenChange,
  registro,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  registro?: RegistroMortalidade | null
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
  } = useForm<MortalidadeFormValues>({
    resolver: zodResolver(mortalidadeSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        registro
          ? {
              loteId: registro.loteId,
              quantidade: registro.quantidade,
              data: registro.data,
              causa: registro.causa,
              observacoes: registro.observacoes ?? undefined,
            }
          : undefined,
      )
    }
  }, [open, registro, reset])

  const mutation = useMutation({
    mutationFn: (values: MortalidadeFormValues) =>
      isEditing ? mortalidadeApi.atualizar(registro!.id, values) : mortalidadeApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mortalidade'] })
      toast.success(isEditing ? 'Registro atualizado.' : 'Registro criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o registro.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar registro de exclusão' : 'Novo registro de exclusão'}</DialogTitle>
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
              <Label htmlFor="quantidade">Quantidade</Label>
              <Input id="quantidade" type="number" step="1" {...register('quantidade')} />
              {errors.quantidade && <p className="text-xs text-destructive">{errors.quantidade.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="data">Data</Label>
              <Input id="data" type="date" {...register('data')} />
              {errors.data && <p className="text-xs text-destructive">{errors.data.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label>Causa</Label>
              <Select value={watch('causa')} onValueChange={(v) => setValue('causa', v as MortalidadeFormValues['causa'])}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione a causa" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(CAUSA_EXCLUSAO_LABELS).map(([valor, label]) => (
                    <SelectItem key={valor} value={valor}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.causa && <p className="text-xs text-destructive">{errors.causa.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="observacoes">Observações</Label>
              <textarea
                id="observacoes"
                rows={3}
                className={cn(
                  'flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm ' +
                    'transition-colors placeholder:text-muted-foreground focus-visible:outline-none ' +
                    'focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50',
                )}
                {...register('observacoes')}
              />
              {errors.observacoes && <p className="text-xs text-destructive">{errors.observacoes.message}</p>}
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

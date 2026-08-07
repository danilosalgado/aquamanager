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
import { qualidadeAguaSchema, type QualidadeAguaFormValues } from '../schemas/qualidade-agua-schema'
import { qualidadeAguaApi, type RegistroQualidadeAgua } from '../api/qualidade-agua-api'
import { tanquesApi } from '@/features/tanques/api/tanques-api'
import { extractErrorMessage } from '@/lib/api-client'

function toDatetimeLocalInput(iso?: string | null): string {
  if (!iso) return ''
  const date = new Date(iso)
  const offset = date.getTimezoneOffset()
  const local = new Date(date.getTime() - offset * 60000)
  return local.toISOString().slice(0, 16)
}

const parametros: { key: keyof QualidadeAguaFormValues; label: string }[] = [
  { key: 'temperatura', label: 'Temperatura (°C)' },
  { key: 'ph', label: 'pH' },
  { key: 'oxigenioDissolvido', label: 'Oxigênio dissolvido (mg/L)' },
  { key: 'amonia', label: 'Amônia (mg/L)' },
  { key: 'nitrito', label: 'Nitrito (mg/L)' },
  { key: 'alcalinidade', label: 'Alcalinidade (mg/L)' },
  { key: 'salinidade', label: 'Salinidade (ppt)' },
  { key: 'transparenciaCm', label: 'Transparência (cm)' },
]

export function QualidadeAguaFormDialog({
  open,
  onOpenChange,
  registro,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  registro?: RegistroQualidadeAgua | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!registro

  const { data: tanques } = useQuery({
    queryKey: ['tanques', 'select'],
    queryFn: () => tanquesApi.listar({ size: 100 }),
    enabled: open,
  })

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<QualidadeAguaFormValues>({
    resolver: zodResolver(qualidadeAguaSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        registro
          ? {
              tanqueId: registro.tanqueId,
              medidoEm: toDatetimeLocalInput(registro.medidoEm),
              temperatura: registro.temperatura ?? undefined,
              ph: registro.ph ?? undefined,
              oxigenioDissolvido: registro.oxigenioDissolvido ?? undefined,
              amonia: registro.amonia ?? undefined,
              nitrito: registro.nitrito ?? undefined,
              alcalinidade: registro.alcalinidade ?? undefined,
              salinidade: registro.salinidade ?? undefined,
              transparenciaCm: registro.transparenciaCm ?? undefined,
            }
          : { medidoEm: toDatetimeLocalInput(new Date().toISOString()) },
      )
    }
  }, [open, registro, reset])

  const mutation = useMutation({
    mutationFn: (values: QualidadeAguaFormValues) => {
      const payload = { ...values, medidoEm: new Date(values.medidoEm).toISOString() }
      return isEditing ? qualidadeAguaApi.atualizar(registro!.id, payload) : qualidadeAguaApi.criar(payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['qualidade-agua'] })
      toast.success(isEditing ? 'Registro atualizado.' : 'Registro criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o registro.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar registro' : 'Novo registro de qualidade da água'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label>Tanque</Label>
              <Select value={watch('tanqueId')} onValueChange={(v) => setValue('tanqueId', v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione um tanque" />
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

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="medidoEm">Data e hora da medição</Label>
              <Input id="medidoEm" type="datetime-local" {...register('medidoEm')} />
              {errors.medidoEm && <p className="text-xs text-destructive">{errors.medidoEm.message}</p>}
            </div>

            {parametros.map(({ key, label }) => (
              <div key={key} className="space-y-1.5">
                <Label htmlFor={key}>{label}</Label>
                <Input id={key} type="number" step="0.01" {...register(key)} />
                {errors[key] && <p className="text-xs text-destructive">{errors[key]?.message as string}</p>}
              </div>
            ))}
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

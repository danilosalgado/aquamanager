import { useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Trash2 } from 'lucide-react'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { cn, formatNumber } from '@/lib/utils'
import { crescimentoSchema, type CrescimentoFormValues } from '../schemas/crescimento-schema'
import { crescimentoApi, type RegistroCrescimento } from '../api/crescimento-api'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { extractErrorMessage } from '@/lib/api-client'

type TipoAmostra = 'TOTAL' | 'INDIVIDUAL'

function calcularAmostra(pesos: string[]) {
  const valores = pesos
    .map((p) => Number(p.replace(',', '.')))
    .filter((v) => Number.isFinite(v) && v > 0)
  const quantidade = valores.length
  const media = quantidade > 0 ? valores.reduce((soma, v) => soma + v, 0) / quantidade : 0
  const desvioPadrao = quantidade > 1
    ? Math.sqrt(valores.reduce((soma, v) => soma + (v - media) ** 2, 0) / quantidade)
    : 0
  const cv = media > 0 ? (desvioPadrao / media) * 100 : 0
  return { quantidade, media, cv }
}

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
    resetField,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<CrescimentoFormValues>({
    resolver: zodResolver(crescimentoSchema),
  })

  const [tipoAmostra, setTipoAmostra] = useState<TipoAmostra>('TOTAL')
  const [pesosIndividuais, setPesosIndividuais] = useState<string[]>([''])

  useEffect(() => {
    if (open) {
      setTipoAmostra('TOTAL')
      setPesosIndividuais([''])
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

  const amostraIndividual = useMemo(() => calcularAmostra(pesosIndividuais), [pesosIndividuais])

  useEffect(() => {
    if (tipoAmostra !== 'INDIVIDUAL') return
    setValue('pesoMedioG', Number(amostraIndividual.media.toFixed(2)), { shouldValidate: true })
    setValue('quantidadeAmostra', amostraIndividual.quantidade, { shouldValidate: true })
  }, [tipoAmostra, amostraIndividual, setValue])

  function trocarTipoAmostra(tipo: TipoAmostra) {
    if (tipo === tipoAmostra) return
    setTipoAmostra(tipo)
    if (tipo === 'TOTAL') {
      resetField('pesoMedioG', { defaultValue: undefined })
      resetField('quantidadeAmostra', { defaultValue: undefined })
    } else {
      setPesosIndividuais([''])
    }
  }

  function adicionarPeixe() {
    setPesosIndividuais((prev) => [...prev, ''])
  }

  function removerPeixe(index: number) {
    setPesosIndividuais((prev) => (prev.length === 1 ? prev : prev.filter((_, i) => i !== index)))
  }

  function atualizarPeso(index: number, valor: string) {
    setPesosIndividuais((prev) => prev.map((p, i) => (i === index ? valor : p)))
  }

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

  function onSubmit(values: CrescimentoFormValues) {
    if (tipoAmostra === 'INDIVIDUAL' && amostraIndividual.quantidade === 0) {
      toast.error('Informe o peso de ao menos um peixe.')
      return
    }
    mutation.mutate(values)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar registro de biometria' : 'Novo registro de biometria'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4">
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

            <div className="col-span-2 space-y-1.5">
              <Label>Tipo de biometria</Label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => trocarTipoAmostra('TOTAL')}
                  className={cn(
                    'rounded-md border px-3 py-2 text-sm font-medium transition-colors',
                    tipoAmostra === 'TOTAL'
                      ? 'border-primary bg-primary text-primary-foreground'
                      : 'border-input bg-background hover:bg-accent',
                  )}
                >
                  Amostra total
                </button>
                <button
                  type="button"
                  onClick={() => trocarTipoAmostra('INDIVIDUAL')}
                  className={cn(
                    'rounded-md border px-3 py-2 text-sm font-medium transition-colors',
                    tipoAmostra === 'INDIVIDUAL'
                      ? 'border-primary bg-primary text-primary-foreground'
                      : 'border-input bg-background hover:bg-accent',
                  )}
                >
                  Individual
                </button>
              </div>
              <p className="text-xs text-muted-foreground">
                {tipoAmostra === 'TOTAL'
                  ? 'Informe o peso médio da amostra.'
                  : 'Informe o peso individual de cada peixe — a média é calculada automaticamente.'}
              </p>
            </div>

            {tipoAmostra === 'TOTAL' ? (
              <>
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
              </>
            ) : (
              <div className="col-span-2 space-y-2">
                <div className="flex flex-wrap items-center justify-between gap-1">
                  <Label>Pesos individuais (g)</Label>
                  <span className="text-xs text-muted-foreground">
                    {amostraIndividual.quantidade} peixes · média: {formatNumber(amostraIndividual.media, 1)} g · CV:{' '}
                    {formatNumber(amostraIndividual.cv, 1)}%
                  </span>
                </div>
                <div className="max-h-52 space-y-2 overflow-y-auto pr-1">
                  {pesosIndividuais.map((peso, index) => (
                    <div key={index} className="flex items-center gap-2">
                      <span className="w-6 shrink-0 text-right text-xs text-muted-foreground">{index + 1}.</span>
                      <Input
                        type="number"
                        step="0.1"
                        placeholder="0,0"
                        value={peso}
                        onChange={(e) => atualizarPeso(index, e.target.value)}
                      />
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 shrink-0"
                        onClick={() => removerPeixe(index)}
                        disabled={pesosIndividuais.length === 1}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
                <Button type="button" variant="outline" className="w-full" onClick={adicionarPeixe}>
                  <Plus className="h-4 w-4" /> Adicionar peixe
                </Button>
                {errors.pesoMedioG && <p className="text-xs text-destructive">{errors.pesoMedioG.message}</p>}
              </div>
            )}

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

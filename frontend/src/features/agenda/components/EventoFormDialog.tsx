import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  Form, FormControl, FormField, FormItem, FormLabel, FormMessage,
} from '@/components/ui/form'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { agendaApi, type Evento } from '../api/agenda-api'
import { eventoSchema, type EventoFormValues, tipoEventoLabels, TipoEventoSchema } from '../schemas/agenda-schema'
import { extractErrorMessage } from '@/lib/api-client'
import { format } from 'date-fns'

interface EventoFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  registro: Evento | null
}

export function EventoFormDialog({ open, onOpenChange, registro }: EventoFormDialogProps) {
  const queryClient = useQueryClient()

  const form = useForm<EventoFormValues>({
    resolver: zodResolver(eventoSchema),
    defaultValues: {
      tipo: 'ALIMENTACAO',
      titulo: '',
      descricao: '',
      dataInicio: format(new Date(), "yyyy-MM-dd'T'HH:mm"),
      concluido: false,
    },
  })

  useEffect(() => {
    if (open) {
      if (registro) {
        form.reset({
          tipo: registro.tipo,
          titulo: registro.titulo,
          descricao: registro.descricao || '',
          dataInicio: registro.dataInicio.substring(0, 16),
          dataFim: registro.dataFim ? registro.dataFim.substring(0, 16) : '',
          concluido: registro.concluido,
        })
      } else {
        form.reset({
          tipo: 'ALIMENTACAO',
          titulo: '',
          descricao: '',
          dataInicio: format(new Date(), "yyyy-MM-dd'T'HH:mm"),
          dataFim: '',
          concluido: false,
        })
      }
    }
  }, [open, registro, form])

  const mutation = useMutation({
    mutationFn: (values: EventoFormValues) => {
      const dataToSave = {
        ...values,
        dataInicio: new Date(values.dataInicio).toISOString(),
        dataFim: values.dataFim ? new Date(values.dataFim).toISOString() : undefined,
      }
      return registro ? agendaApi.atualizar(registro.id, dataToSave) : agendaApi.criar(dataToSave)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['agenda'] })
      toast.success(registro ? 'Evento atualizado.' : 'Evento cadastrado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Erro ao salvar evento.')),
  })

  const removeMutation = useMutation({
    mutationFn: (id: string) => agendaApi.remover(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['agenda'] })
      toast.success('Evento removido.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Erro ao remover evento.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{registro ? 'Editar Evento' : 'Novo Evento'}</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit((d) => mutation.mutate(d))} className="space-y-4 mt-2">
            
            <FormField
              control={form.control}
              name="tipo"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Tipo de evento</FormLabel>
                  <Select onValueChange={field.onChange} defaultValue={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Selecione..." />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {TipoEventoSchema.options.map((tipo) => (
                        <SelectItem key={tipo} value={tipo}>{tipoEventoLabels[tipo]}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="titulo"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Título</FormLabel>
                  <FormControl>
                    <Input placeholder="Ex: Adicionar ração inicial" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="dataInicio"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Data Início</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="dataFim"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Data Fim (Opcional)</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="descricao"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Descrição</FormLabel>
                  <FormControl>
                    <Textarea className="resize-none" placeholder="Detalhes opcionais..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="concluido"
              render={({ field }) => (
                <FormItem className="flex flex-row items-start space-x-3 space-y-0 rounded-md border p-4">
                  <FormControl>
                    <Checkbox checked={field.value} onCheckedChange={field.onChange} />
                  </FormControl>
                  <div className="space-y-1 leading-none">
                    <FormLabel>Marcar como concluído</FormLabel>
                  </div>
                </FormItem>
              )}
            />

            <DialogFooter className="pt-4 flex sm:justify-between">
              {registro ? (
                <Button 
                  type="button" 
                  variant="destructive" 
                  onClick={() => removeMutation.mutate(registro.id)}
                  loading={removeMutation.isPending}
                  disabled={mutation.isPending}
                >
                  Remover
                </Button>
              ) : <div />}
              <div className="flex gap-2">
                <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Cancelar</Button>
                <Button type="submit" loading={mutation.isPending}>Salvar</Button>
              </div>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}

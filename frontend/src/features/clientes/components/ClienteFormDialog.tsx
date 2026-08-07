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
import { clienteSchema, type ClienteFormValues } from '../schemas/cliente-schema'
import { clientesApi, type Cliente } from '../api/clientes-api'
import { extractErrorMessage } from '@/lib/api-client'

export function ClienteFormDialog({
  open,
  onOpenChange,
  cliente,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  cliente?: Cliente | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!cliente

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ClienteFormValues>({
    resolver: zodResolver(clienteSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        cliente
          ? {
              nome: cliente.nome,
              documento: cliente.documento ?? undefined,
              telefone: cliente.telefone ?? undefined,
              email: cliente.email ?? undefined,
              endereco: cliente.endereco ?? undefined,
              observacoes: cliente.observacoes ?? undefined,
            }
          : { nome: '' },
      )
    }
  }, [open, cliente, reset])

  const mutation = useMutation({
    mutationFn: (values: ClienteFormValues) =>
      isEditing ? clientesApi.atualizar(cliente!.id, values) : clientesApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clientes'] })
      toast.success(isEditing ? 'Cliente atualizado.' : 'Cliente criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o cliente.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar cliente' : 'Novo cliente'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nome">Nome</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="documento">Documento (CPF/CNPJ)</Label>
              <Input id="documento" {...register('documento')} />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="telefone">Telefone</Label>
              <Input id="telefone" {...register('telefone')} />
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="email">E-mail</Label>
              <Input id="email" type="email" {...register('email')} />
              {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="endereco">Endereço</Label>
              <Input id="endereco" {...register('endereco')} />
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
              {isEditing ? 'Salvar alterações' : 'Criar cliente'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

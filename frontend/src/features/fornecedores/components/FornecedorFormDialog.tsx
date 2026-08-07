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
import { cn } from '@/lib/utils'
import { fornecedorSchema, type FornecedorFormValues } from '../schemas/fornecedor-schema'
import { fornecedoresApi, type Fornecedor } from '../api/fornecedores-api'
import { extractErrorMessage } from '@/lib/api-client'

export function FornecedorFormDialog({
  open,
  onOpenChange,
  fornecedor,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  fornecedor?: Fornecedor | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!fornecedor

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FornecedorFormValues>({
    resolver: zodResolver(fornecedorSchema),
  })

  useEffect(() => {
    if (open) {
      reset(
        fornecedor
          ? {
              nome: fornecedor.nome,
              documento: fornecedor.documento ?? undefined,
              telefone: fornecedor.telefone ?? undefined,
              email: fornecedor.email ?? undefined,
              produtosFornecidos: fornecedor.produtosFornecidos ?? undefined,
              observacoes: fornecedor.observacoes ?? undefined,
            }
          : { nome: '' },
      )
    }
  }, [open, fornecedor, reset])

  const mutation = useMutation({
    mutationFn: (values: FornecedorFormValues) =>
      isEditing ? fornecedoresApi.atualizar(fornecedor!.id, values) : fornecedoresApi.criar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fornecedores'] })
      toast.success(isEditing ? 'Fornecedor atualizado.' : 'Fornecedor criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o fornecedor.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar fornecedor' : 'Novo fornecedor'}</DialogTitle>
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
              <Label htmlFor="produtosFornecidos">Produtos fornecidos</Label>
              <textarea
                id="produtosFornecidos"
                rows={3}
                className={cn(
                  'flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm ' +
                    'transition-colors placeholder:text-muted-foreground focus-visible:outline-none ' +
                    'focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50',
                )}
                {...register('produtosFornecidos')}
              />
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
              {isEditing ? 'Salvar alterações' : 'Criar fornecedor'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

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
import { Switch } from '@/components/ui/switch'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { usuarioEditSchema, roleLabels, type UsuarioEditFormValues } from '@/features/usuarios/schemas/usuario-schema'
import { adminUsuariosApi, type UsuarioAdmin } from '../api/admin-usuarios-api'
import { extractErrorMessage } from '@/lib/api-client'
import type { Role } from '@/types/auth'

export function AdminUsuarioEditDialog({
  open,
  onOpenChange,
  usuario,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  usuario: UsuarioAdmin | null
}) {
  const queryClient = useQueryClient()

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<UsuarioEditFormValues>({ resolver: zodResolver(usuarioEditSchema) })

  useEffect(() => {
    if (open && usuario) {
      reset({ nome: usuario.nome, role: usuario.role, ativo: usuario.ativo })
    }
  }, [open, usuario, reset])

  const mutation = useMutation({
    mutationFn: (values: UsuarioEditFormValues) => adminUsuariosApi.atualizar(usuario!.id, values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'usuarios'] })
      toast.success('Usuário atualizado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o usuário.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Editar usuário</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nome">Nome</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label>E-mail</Label>
              <Input value={usuario?.email ?? ''} disabled />
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label>Empresa</Label>
              <Input value={usuario?.empresaNome ?? ''} disabled />
            </div>

            <div className="space-y-1.5">
              <Label>Papel</Label>
              <Select value={watch('role')} onValueChange={(v) => setValue('role', v as Role)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(roleLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.role && <p className="text-xs text-destructive">{errors.role.message}</p>}
            </div>

            <div className="flex items-end space-x-2 pb-1.5">
              <Switch id="ativo" checked={watch('ativo')} onCheckedChange={(v) => setValue('ativo', v)} />
              <Label htmlFor="ativo">Ativo</Label>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              Salvar alterações
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

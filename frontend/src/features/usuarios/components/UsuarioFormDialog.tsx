import { useEffect } from 'react'
import { useForm, type Resolver } from 'react-hook-form'
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
import { usuarioCreateSchema, usuarioEditSchema, roleLabels } from '../schemas/usuario-schema'
import { usuariosApi } from '../api/usuarios-api'
import { extractErrorMessage } from '@/lib/api-client'
import type { Role, Usuario } from '@/types/auth'

interface UsuarioFormValues {
  nome: string
  email: string
  senha: string
  role: Role
  ativo: boolean
}

export function UsuarioFormDialog({
  open,
  onOpenChange,
  usuario,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  usuario?: Usuario | null
}) {
  const queryClient = useQueryClient()
  const isEditing = !!usuario

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<UsuarioFormValues>({
    resolver: (isEditing
      ? zodResolver(usuarioEditSchema)
      : zodResolver(usuarioCreateSchema)) as unknown as Resolver<UsuarioFormValues>,
  })

  useEffect(() => {
    if (open) {
      reset(
        usuario
          ? {
              nome: usuario.nome,
              email: usuario.email,
              senha: '',
              role: usuario.role,
              ativo: usuario.ativo,
            }
          : { nome: '', email: '', senha: '', role: 'FUNCIONARIO', ativo: true },
      )
    }
  }, [open, usuario, reset])

  const mutation = useMutation({
    mutationFn: (values: UsuarioFormValues) =>
      isEditing
        ? usuariosApi.atualizar(usuario!.id, { nome: values.nome, role: values.role, ativo: values.ativo })
        : usuariosApi.criar({ nome: values.nome, email: values.email, senha: values.senha, role: values.role }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
      toast.success(isEditing ? 'Usuário atualizado.' : 'Usuário criado.')
      onOpenChange(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível salvar o usuário.')),
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar usuário' : 'Novo usuário'}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nome">Nome</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>

            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="email">E-mail</Label>
              {isEditing ? (
                <Input id="email" value={usuario?.email} disabled />
              ) : (
                <>
                  <Input id="email" type="email" {...register('email')} />
                  {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
                </>
              )}
            </div>

            {!isEditing && (
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="senha">Senha</Label>
                <Input id="senha" type="password" {...register('senha')} />
                {errors.senha && <p className="text-xs text-destructive">{errors.senha.message}</p>}
              </div>
            )}

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

            {isEditing && (
              <div className="flex items-end space-x-2 pb-1.5">
                <Switch id="ativo" checked={watch('ativo')} onCheckedChange={(v) => setValue('ativo', v)} />
                <Label htmlFor="ativo">Ativo</Label>
              </div>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancelar
            </Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              {isEditing ? 'Salvar alterações' : 'Criar usuário'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

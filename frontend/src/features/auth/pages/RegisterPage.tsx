import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { AuthLayout } from '../components/AuthLayout'
import { registerSchema, type RegisterFormValues } from '../schemas/auth-schemas'
import { authApi } from '../api/auth-api'
import { useAuthStore } from '@/stores/auth-store'
import { extractErrorMessage } from '@/lib/api-client'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'

export default function RegisterPage() {
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) })

  async function onSubmit(values: RegisterFormValues) {
    setLoading(true)
    try {
      const result = await authApi.register(values)
      setSession(result.accessToken, result.usuario)
      toast.success('Conta criada! Você tem 14 dias grátis para explorar a plataforma.')
      navigate('/', { replace: true })
    } catch (error) {
      toast.error(extractErrorMessage(error, 'Não foi possível concluir o cadastro.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout title="Comece seu teste grátis de 14 dias" description="Sem cartão de crédito. Cancele quando quiser." wide>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div>
          <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-muted-foreground">Sua empresa</p>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5 sm:col-span-2">
              <Label htmlFor="nomeEmpresa">Nome da empresa / fazenda</Label>
              <Input id="nomeEmpresa" {...register('nomeEmpresa')} />
              {errors.nomeEmpresa && <p className="text-xs text-destructive">{errors.nomeEmpresa.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="documento">CPF ou CNPJ</Label>
              <Input id="documento" {...register('documento')} />
              {errors.documento && <p className="text-xs text-destructive">{errors.documento.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="emailEmpresa">E-mail da empresa</Label>
              <Input id="emailEmpresa" type="email" {...register('emailEmpresa')} />
              {errors.emailEmpresa && <p className="text-xs text-destructive">{errors.emailEmpresa.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="telefone">Telefone</Label>
              <Input id="telefone" {...register('telefone')} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cidade">Cidade</Label>
              <Input id="cidade" {...register('cidade')} />
            </div>
          </div>
        </div>

        <Separator />

        <div>
          <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-muted-foreground">Seu acesso</p>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5 sm:col-span-2">
              <Label htmlFor="nomeUsuario">Seu nome</Label>
              <Input id="nomeUsuario" {...register('nomeUsuario')} />
              {errors.nomeUsuario && <p className="text-xs text-destructive">{errors.nomeUsuario.message}</p>}
            </div>
            <div className="space-y-1.5 sm:col-span-2">
              <Label htmlFor="emailUsuario">Seu e-mail (login)</Label>
              <Input id="emailUsuario" type="email" {...register('emailUsuario')} />
              {errors.emailUsuario && <p className="text-xs text-destructive">{errors.emailUsuario.message}</p>}
            </div>
            <div className="space-y-1.5 sm:col-span-2">
              <Label htmlFor="senha">Senha</Label>
              <Input id="senha" type="password" {...register('senha')} />
              {errors.senha && <p className="text-xs text-destructive">{errors.senha.message}</p>}
            </div>
          </div>
        </div>

        <Button type="submit" className="w-full" loading={loading}>
          Criar minha conta
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        Já tem conta?{' '}
        <Link to="/login" className="font-medium text-primary hover:underline">
          Entrar
        </Link>
      </p>
    </AuthLayout>
  )
}

import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { AuthLayout } from '../components/AuthLayout'
import { loginSchema, type LoginFormValues } from '../schemas/auth-schemas'
import { authApi } from '../api/auth-api'
import { useAuthStore } from '@/stores/auth-store'
import { extractErrorCode, extractErrorMessage } from '@/lib/api-client'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const setSession = useAuthStore((s) => s.setSession)
  const [loading, setLoading] = useState(false)
  const [requires2fa, setRequires2fa] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) })

  async function onSubmit(values: LoginFormValues) {
    setLoading(true)
    try {
      const result = await authApi.login(values)
      setSession(result.accessToken, result.usuario)
      const from = (location.state as { from?: Location })?.from?.pathname ?? '/'
      navigate(from, { replace: true })
    } catch (error) {
      const code = extractErrorCode(error)
      if (code === '2FA_REQUIRED') {
        setRequires2fa(true)
        toast.info('Informe o código do seu autenticador de dois fatores.')
      } else {
        toast.error(extractErrorMessage(error, 'Não foi possível entrar. Verifique suas credenciais.'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout title="Entrar na sua conta" description="Acesse o painel da sua fazenda de peixes.">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="email">E-mail</Label>
          <Input id="email" type="email" placeholder="voce@fazenda.com.br" autoComplete="email" {...register('email')} />
          {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
        </div>

        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <Label htmlFor="senha">Senha</Label>
            <Link to="/esqueci-senha" className="text-xs font-medium text-primary hover:underline">
              Esqueceu a senha?
            </Link>
          </div>
          <Input id="senha" type="password" autoComplete="current-password" {...register('senha')} />
          {errors.senha && <p className="text-xs text-destructive">{errors.senha.message}</p>}
        </div>

        {requires2fa && (
          <div className="space-y-1.5">
            <Label htmlFor="codigo2fa">Código de autenticação (2FA)</Label>
            <Input id="codigo2fa" inputMode="numeric" maxLength={6} placeholder="000000" {...register('codigo2fa')} />
          </div>
        )}

        <Button type="submit" className="w-full" loading={loading}>
          Entrar
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        Ainda não tem conta?{' '}
        <Link to="/cadastro" className="font-medium text-primary hover:underline">
          Comece grátis por 14 dias
        </Link>
      </p>
    </AuthLayout>
  )
}

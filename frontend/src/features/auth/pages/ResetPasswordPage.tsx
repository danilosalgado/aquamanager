import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { AuthLayout } from '../components/AuthLayout'
import { resetPasswordSchema, type ResetPasswordFormValues } from '../schemas/auth-schemas'
import { authApi } from '../api/auth-api'
import { extractErrorMessage } from '@/lib/api-client'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({ resolver: zodResolver(resetPasswordSchema) })

  async function onSubmit(values: ResetPasswordFormValues) {
    if (!token) {
      toast.error('Link inválido ou expirado.')
      return
    }
    setLoading(true)
    try {
      await authApi.resetPassword(token, values.novaSenha)
      toast.success('Senha redefinida com sucesso. Faça login novamente.')
      navigate('/login', { replace: true })
    } catch (error) {
      toast.error(extractErrorMessage(error, 'Não foi possível redefinir a senha.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout title="Redefinir senha" description="Escolha uma nova senha para sua conta.">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="novaSenha">Nova senha</Label>
          <Input id="novaSenha" type="password" {...register('novaSenha')} />
          {errors.novaSenha && <p className="text-xs text-destructive">{errors.novaSenha.message}</p>}
        </div>
        <Button type="submit" className="w-full" loading={loading}>
          Redefinir senha
        </Button>
      </form>
      <Link to="/login" className="mt-6 block text-center text-sm font-medium text-primary hover:underline">
        Voltar para o login
      </Link>
    </AuthLayout>
  )
}

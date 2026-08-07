import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { CheckCircle2, Loader2, XCircle } from 'lucide-react'
import { AuthLayout } from '../components/AuthLayout'
import { authApi } from '../api/auth-api'

export default function ConfirmEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')

  useEffect(() => {
    if (!token) {
      setStatus('error')
      return
    }
    authApi
      .confirmEmail(token)
      .then(() => setStatus('success'))
      .catch(() => setStatus('error'))
  }, [token])

  return (
    <AuthLayout title="Confirmação de e-mail">
      <div className="flex flex-col items-center gap-3 rounded-xl border border-border bg-muted/40 p-6 text-center">
        {status === 'loading' && <Loader2 className="h-8 w-8 animate-spin text-primary" />}
        {status === 'success' && (
          <>
            <CheckCircle2 className="h-8 w-8 text-success" />
            <p className="text-sm text-muted-foreground">Seu e-mail foi confirmado com sucesso.</p>
          </>
        )}
        {status === 'error' && (
          <>
            <XCircle className="h-8 w-8 text-destructive" />
            <p className="text-sm text-muted-foreground">Link inválido ou expirado.</p>
          </>
        )}
      </div>
      <Link to="/login" className="mt-6 block text-center text-sm font-medium text-primary hover:underline">
        Ir para o login
      </Link>
    </AuthLayout>
  )
}

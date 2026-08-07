import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { User, Lock, ShieldCheck, Eye, EyeOff, Copy, Check } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { useAuth } from '@/hooks/use-auth'
import { authApi } from '@/features/auth/api/auth-api'
import { extractErrorMessage } from '@/lib/api-client'

/* ---------- Schemas ---------- */

const changePasswordSchema = z
  .object({
    senhaAtual: z.string().min(1, 'Informe a senha atual.'),
    novaSenha: z.string().min(8, 'A nova senha deve ter ao menos 8 caracteres.'),
    confirmacao: z.string().min(1, 'Confirme a nova senha.'),
  })
  .refine((d) => d.novaSenha === d.confirmacao, {
    message: 'As senhas não conferem.',
    path: ['confirmacao'],
  })

type ChangePasswordValues = z.infer<typeof changePasswordSchema>

const roleLabels: Record<string, string> = {
  ADMINISTRADOR: 'Administrador',
  GERENTE: 'Gerente',
  FUNCIONARIO: 'Funcionário',
  CONSULTOR: 'Consultor',
}

/* ---------- Page ---------- */

export default function PerfilPage() {
  return (
    <div>
      <PageHeader title="Meu Perfil" description="Gerencie seus dados, segurança e autenticação em dois fatores." />

      <Tabs defaultValue="dados">
        <TabsList>
          <TabsTrigger value="dados">Dados pessoais</TabsTrigger>
          <TabsTrigger value="senha">Alterar senha</TabsTrigger>
          <TabsTrigger value="2fa">Autenticação 2FA</TabsTrigger>
        </TabsList>

        <TabsContent value="dados">
          <DadosTab />
        </TabsContent>

        <TabsContent value="senha">
          <SenhaTab />
        </TabsContent>

        <TabsContent value="2fa">
          <TwoFactorTab />
        </TabsContent>
      </Tabs>
    </div>
  )
}

/* ---------- Dados pessoais ---------- */

function DadosTab() {
  const { usuario } = useAuth()

  return (
    <Card className="mt-4">
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
            <User className="h-6 w-6 text-primary" />
          </div>
          <div>
            <CardTitle>{usuario?.nome}</CardTitle>
            <CardDescription>{usuario?.email}</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label className="text-muted-foreground">Nome</Label>
            <p className="text-sm font-medium">{usuario?.nome ?? '—'}</p>
          </div>
          <div className="space-y-1.5">
            <Label className="text-muted-foreground">E-mail</Label>
            <p className="text-sm font-medium">{usuario?.email ?? '—'}</p>
          </div>
          <div className="space-y-1.5">
            <Label className="text-muted-foreground">Função</Label>
            <div>
              <Badge variant="secondary">{roleLabels[usuario?.role ?? ''] ?? usuario?.role}</Badge>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label className="text-muted-foreground">E-mail confirmado</Label>
            <div>
              {usuario?.emailConfirmado ? (
                <Badge variant="success">Confirmado</Badge>
              ) : (
                <Badge variant="warning">Pendente</Badge>
              )}
            </div>
          </div>
          <div className="space-y-1.5">
            <Label className="text-muted-foreground">2FA</Label>
            <div>
              {usuario?.twoFactorEnabled ? (
                <Badge variant="success">Ativado</Badge>
              ) : (
                <Badge variant="secondary">Desativado</Badge>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

/* ---------- Alterar senha ---------- */

function SenhaTab() {
  const [showPasswords, setShowPasswords] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ChangePasswordValues>({
    resolver: zodResolver(changePasswordSchema),
  })

  const mutation = useMutation({
    mutationFn: (values: ChangePasswordValues) =>
      authApi.changePassword(values.senhaAtual, values.novaSenha),
    onSuccess: () => {
      toast.success('Senha alterada com sucesso.')
      reset()
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível alterar a senha.')),
  })

  return (
    <Card className="mt-4">
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
            <Lock className="h-5 w-5 text-primary" />
          </div>
          <div>
            <CardTitle>Alterar senha</CardTitle>
            <CardDescription>Recomendamos usar pelo menos 12 caracteres com letras, números e símbolos.</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <form id="change-password-form" onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4 max-w-md">
          <div className="space-y-1.5">
            <Label htmlFor="senhaAtual">Senha atual</Label>
            <div className="relative">
              <Input
                id="senhaAtual"
                type={showPasswords ? 'text' : 'password'}
                autoComplete="current-password"
                {...register('senhaAtual')}
              />
              <button
                type="button"
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                onClick={() => setShowPasswords((p) => !p)}
                tabIndex={-1}
              >
                {showPasswords ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {errors.senhaAtual && <p className="text-xs text-destructive">{errors.senhaAtual.message}</p>}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="novaSenha">Nova senha</Label>
            <Input
              id="novaSenha"
              type={showPasswords ? 'text' : 'password'}
              autoComplete="new-password"
              {...register('novaSenha')}
            />
            {errors.novaSenha && <p className="text-xs text-destructive">{errors.novaSenha.message}</p>}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="confirmacao">Confirmar nova senha</Label>
            <Input
              id="confirmacao"
              type={showPasswords ? 'text' : 'password'}
              autoComplete="new-password"
              {...register('confirmacao')}
            />
            {errors.confirmacao && <p className="text-xs text-destructive">{errors.confirmacao.message}</p>}
          </div>
        </form>
      </CardContent>
      <CardFooter>
        <Button type="submit" form="change-password-form" loading={mutation.isPending}>
          Alterar senha
        </Button>
      </CardFooter>
    </Card>
  )
}

/* ---------- 2FA ---------- */

function TwoFactorTab() {
  const { usuario } = useAuth()
  const queryClient = useQueryClient()
  const is2FaEnabled = usuario?.twoFactorEnabled ?? false

  const [setupData, setSetupData] = useState<{ secret: string; qrCodeDataUri: string } | null>(null)
  const [codigo, setCodigo] = useState('')
  const [copied, setCopied] = useState(false)
  const [showDisableDialog, setShowDisableDialog] = useState(false)
  const [disablePassword, setDisablePassword] = useState('')

  const setupMutation = useMutation({
    mutationFn: () => authApi.setup2Fa(),
    onSuccess: (data) => setSetupData(data),
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível iniciar a configuração do 2FA.')),
  })

  const enableMutation = useMutation({
    mutationFn: (code: string) => authApi.enable2Fa(code),
    onSuccess: () => {
      toast.success('Autenticação em dois fatores ativada!')
      setSetupData(null)
      setCodigo('')
      queryClient.invalidateQueries({ queryKey: ['auth'] })
      // Force re-render with updated user; in practice the next /auth/me call updates this
      window.location.reload()
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Código inválido. Tente novamente.')),
  })

  const disableMutation = useMutation({
    mutationFn: (senha: string) => authApi.disable2Fa(senha),
    onSuccess: () => {
      toast.success('Autenticação em dois fatores desativada.')
      setShowDisableDialog(false)
      setDisablePassword('')
      window.location.reload()
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível desativar o 2FA.')),
  })

  function handleCopySecret() {
    if (setupData?.secret) {
      navigator.clipboard.writeText(setupData.secret)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <Card className="mt-4">
      <CardHeader>
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
            <ShieldCheck className="h-5 w-5 text-primary" />
          </div>
          <div>
            <CardTitle>Autenticação em dois fatores (2FA)</CardTitle>
            <CardDescription>
              Adicione uma camada extra de segurança à sua conta usando um aplicativo autenticador
              (Google Authenticator, Authy, etc.).
            </CardDescription>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {is2FaEnabled && !setupData && (
          <div className="rounded-lg border border-success/30 bg-success/5 p-4">
            <div className="flex items-center gap-2">
              <ShieldCheck className="h-5 w-5 text-success" />
              <p className="text-sm font-medium text-success">
                A autenticação em dois fatores está ativada na sua conta.
              </p>
            </div>
          </div>
        )}

        {!is2FaEnabled && !setupData && (
          <div className="rounded-lg border border-warning/30 bg-warning/5 p-4">
            <p className="text-sm text-muted-foreground">
              A autenticação em dois fatores está <strong>desativada</strong>. Recomendamos ativá-la para
              proteger melhor sua conta.
            </p>
          </div>
        )}

        {/* QR Code setup flow */}
        {setupData && (
          <div className="space-y-4">
            <Separator />
            <p className="text-sm text-muted-foreground">
              Escaneie o QR Code abaixo no seu aplicativo autenticador. Se preferir, insira a chave secreta manualmente.
            </p>

            <div className="flex flex-col items-center gap-4 sm:flex-row sm:items-start">
              <div className="rounded-lg border bg-white p-3">
                <img
                  src={setupData.qrCodeDataUri}
                  alt="QR Code para configuração do 2FA"
                  className="h-48 w-48"
                />
              </div>

              <div className="space-y-3 flex-1">
                <div className="space-y-1.5">
                  <Label className="text-muted-foreground">Chave secreta</Label>
                  <div className="flex items-center gap-2">
                    <code className="rounded bg-muted px-2 py-1 text-xs font-mono break-all">
                      {setupData.secret}
                    </code>
                    <button
                      type="button"
                      onClick={handleCopySecret}
                      className="shrink-0 text-muted-foreground hover:text-foreground"
                    >
                      {copied ? <Check className="h-4 w-4 text-success" /> : <Copy className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                <div className="space-y-1.5 max-w-xs">
                  <Label htmlFor="codigo2fa">Código de verificação</Label>
                  <Input
                    id="codigo2fa"
                    placeholder="000000"
                    maxLength={6}
                    value={codigo}
                    onChange={(e) => setCodigo(e.target.value.replace(/\D/g, ''))}
                    autoComplete="one-time-code"
                  />
                  <p className="text-xs text-muted-foreground">
                    Digite o código de 6 dígitos gerado pelo seu app autenticador.
                  </p>
                </div>

                <Button
                  onClick={() => enableMutation.mutate(codigo)}
                  loading={enableMutation.isPending}
                  disabled={codigo.length !== 6}
                >
                  Ativar 2FA
                </Button>
              </div>
            </div>
          </div>
        )}
      </CardContent>

      <CardFooter className="flex gap-2">
        {!is2FaEnabled && !setupData && (
          <Button onClick={() => setupMutation.mutate()} loading={setupMutation.isPending}>
            Configurar 2FA
          </Button>
        )}

        {is2FaEnabled && (
          <Button variant="destructive" onClick={() => setShowDisableDialog(true)}>
            Desativar 2FA
          </Button>
        )}

        {setupData && (
          <Button variant="ghost" onClick={() => { setSetupData(null); setCodigo('') }}>
            Cancelar
          </Button>
        )}
      </CardFooter>

      {/* Disable 2FA confirmation dialog */}
      <ConfirmDialog
        open={showDisableDialog}
        onOpenChange={(open) => {
          setShowDisableDialog(open)
          if (!open) setDisablePassword('')
        }}
        title="Desativar autenticação em dois fatores?"
        description="Isso removerá a camada extra de segurança da sua conta. Para confirmar, digite sua senha abaixo."
        confirmLabel="Desativar"
        loading={disableMutation.isPending}
        onConfirm={() => disableMutation.mutate(disablePassword)}
      >
        <div className="space-y-1.5 mt-3">
          <Label htmlFor="disable-2fa-password">Sua senha</Label>
          <Input
            id="disable-2fa-password"
            type="password"
            value={disablePassword}
            onChange={(e) => setDisablePassword(e.target.value)}
            autoComplete="current-password"
          />
        </div>
      </ConfirmDialog>
    </Card>
  )
}

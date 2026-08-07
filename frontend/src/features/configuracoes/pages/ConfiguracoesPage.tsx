import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import { Check, X, Building2, CreditCard } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { empresaApi, billingApi, planosApi, type EmpresaStatus, type Plano } from '../api/empresa-api'
import { extractErrorMessage } from '@/lib/api-client'
import { formatCurrency } from '@/lib/utils'

const empresaFormSchema = z.object({
  nome: z.string().min(2, 'Informe o nome da empresa.').max(150),
  endereco: z.string().optional(),
  cidade: z.string().optional(),
  estado: z.string().max(2).optional(),
  telefone: z.string().optional(),
  email: z.string().email('Informe um e-mail válido.').optional().or(z.literal('')),
})
type EmpresaFormValues = z.infer<typeof empresaFormSchema>

const statusLabels: Record<EmpresaStatus, string> = {
  TRIAL: 'Período de teste',
  ATIVA: 'Ativa',
  INADIMPLENTE: 'Inadimplente',
  CANCELADA: 'Cancelada',
  BLOQUEADA: 'Bloqueada',
}

function statusVariant(status: EmpresaStatus) {
  if (status === 'ATIVA') return 'success' as const
  if (status === 'TRIAL') return 'warning' as const
  if (status === 'INADIMPLENTE' || status === 'BLOQUEADA') return 'destructive' as const
  return 'secondary' as const
}

function diasRestantes(trialEndsAt: string | null): number | null {
  if (!trialEndsAt) return null
  const diff = new Date(trialEndsAt).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / 86_400_000))
}

const recursoLabels: Record<string, string> = {
  dashboard: 'Dashboard',
  financeiro: 'Financeiro',
  estoque: 'Estoque',
  ia: 'Assistente de IA',
  agenda: 'Agenda',
  relatorios: 'Relatórios',
  api: 'API pública',
  whiteLabel: 'White label',
  backupAutomatico: 'Backup automático',
}

function formatLimite(limite: number | null, singular: string, plural: string): string {
  if (limite == null) return `${plural} ilimitados`
  return limite === 1 ? `Até 1 ${singular}` : `Até ${limite} ${plural}`
}

export default function ConfiguracoesPage() {
  const [searchParams] = useSearchParams()
  // O redirect de volta do checkout do Stripe (successUrl/cancelUrl) aponta para
  // /configuracoes puro — sem isso, o retorno cairia na aba "Empresa" por padrão e o
  // toast de confirmação (tratado dentro de PlanoTab) nunca seria exibido.
  const abaInicial = searchParams.has('checkout') ? 'plano' : 'empresa'

  return (
    <div>
      <PageHeader title="Configurações" description="Dados da empresa e gestão do plano de assinatura." />

      <Tabs defaultValue={abaInicial}>
        <TabsList>
          <TabsTrigger value="empresa">Empresa</TabsTrigger>
          <TabsTrigger value="plano">Plano &amp; Assinatura</TabsTrigger>
        </TabsList>

        <TabsContent value="empresa">
          <EmpresaTab />
        </TabsContent>

        <TabsContent value="plano">
          <PlanoTab />
        </TabsContent>
      </Tabs>
    </div>
  )
}

function EmpresaTab() {
  const queryClient = useQueryClient()
  const { data: empresa, isLoading } = useQuery({
    queryKey: ['empresa', 'me'],
    queryFn: () => empresaApi.buscarMinhaEmpresa(),
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<EmpresaFormValues>({
    resolver: zodResolver(empresaFormSchema),
  })

  useEffect(() => {
    if (empresa) {
      reset({
        nome: empresa.nome,
        endereco: empresa.endereco ?? undefined,
        cidade: empresa.cidade ?? undefined,
        estado: empresa.estado ?? undefined,
        telefone: empresa.telefone ?? undefined,
        email: empresa.email ?? undefined,
      })
    }
  }, [empresa, reset])

  const mutation = useMutation({
    mutationFn: (values: EmpresaFormValues) => empresaApi.atualizar(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['empresa', 'me'] })
      toast.success('Dados da empresa atualizados.')
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível atualizar a empresa.')),
  })

  if (isLoading || !empresa) {
    return (
      <div className="mt-4 space-y-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-10 w-full" />
        ))}
      </div>
    )
  }

  const dias = diasRestantes(empresa.trialEndsAt)

  return (
    <Card className="mt-4">
      <CardHeader className="flex flex-row items-start justify-between space-y-0">
        <div>
          <CardTitle>Dados da empresa</CardTitle>
          <CardDescription>Essas informações aparecem em documentos e notificações.</CardDescription>
        </div>
        <div className="flex flex-col items-end gap-1">
          <Badge variant={statusVariant(empresa.status)}>{statusLabels[empresa.status]}</Badge>
          {empresa.status === 'TRIAL' && dias !== null && (
            <span className="text-xs text-muted-foreground">
              {dias === 0 ? 'Encerra hoje' : `${dias} dia(s) restante(s)`}
            </span>
          )}
        </div>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit((v) => mutation.mutate(v))} className="grid gap-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2 space-y-1.5">
              <Label htmlFor="nome">Nome da empresa</Label>
              <Input id="nome" {...register('nome')} />
              {errors.nome && <p className="text-xs text-destructive">{errors.nome.message}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="documento">Documento (CNPJ/CPF)</Label>
              <Input id="documento" value={empresa.documento} disabled />
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

            <div className="space-y-1.5">
              <Label htmlFor="cidade">Cidade</Label>
              <Input id="cidade" {...register('cidade')} />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="estado">Estado (UF)</Label>
              <Input id="estado" maxLength={2} {...register('estado')} />
            </div>
          </div>

          <div className="flex justify-end">
            <Button type="submit" loading={isSubmitting || mutation.isPending}>
              Salvar alterações
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

function PlanoTab() {
  const queryClient = useQueryClient()
  const [cancelando, setCancelando] = useState(false)
  const [searchParams, setSearchParams] = useSearchParams()

  const { data: assinatura, isLoading: carregandoAssinatura } = useQuery({
    queryKey: ['billing', 'assinatura'],
    queryFn: () => billingApi.assinaturaAtual(),
  })

  const { data: planos, isLoading: carregandoPlanos } = useQuery({
    queryKey: ['planos'],
    queryFn: () => planosApi.listar(),
  })

  useEffect(() => {
    const checkout = searchParams.get('checkout')
    if (!checkout) return

    if (checkout === 'success') {
      toast.success('Pagamento confirmado! Sua assinatura está sendo ativada — isso leva só alguns segundos.')
      queryClient.invalidateQueries({ queryKey: ['billing', 'assinatura'] })
      queryClient.invalidateQueries({ queryKey: ['empresa', 'me'] })
    } else if (checkout === 'cancel') {
      toast.info('Checkout cancelado — nenhuma cobrança foi feita.')
    }

    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev)
        next.delete('checkout')
        return next
      },
      { replace: true },
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams])

  const checkoutMutation = useMutation({
    mutationFn: () => billingApi.criarCheckoutSession(),
    onSuccess: (url) => {
      window.location.href = url
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível iniciar o checkout.')),
  })

  const portalMutation = useMutation({
    mutationFn: () => billingApi.abrirPortal(),
    onSuccess: (url) => {
      window.location.href = url
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível abrir o portal de gerenciamento.')),
  })

  const cancelarMutation = useMutation({
    mutationFn: () => billingApi.cancelar(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['billing', 'assinatura'] })
      queryClient.invalidateQueries({ queryKey: ['empresa', 'me'] })
      toast.success('Assinatura cancelada.')
      setCancelando(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error, 'Não foi possível cancelar a assinatura.')),
  })

  if (carregandoAssinatura || carregandoPlanos) {
    return (
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        {Array.from({ length: 2 }).map((_, i) => (
          <Skeleton key={i} className="h-52 w-full" />
        ))}
      </div>
    )
  }

  const ativa = assinatura?.status === 'ATIVA'
  const plano = planos?.[0]

  return (
    <div className="mt-4 space-y-6">
      {assinatura && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Building2 className="h-4 w-4 text-primary" /> Status da assinatura
            </CardTitle>
            <CardDescription>
              Plano {assinatura.plano.nome} · {formatCurrency(assinatura.plano.precoMensal)}/mês ·{' '}
              {statusLabels[assinatura.status as EmpresaStatus] ?? assinatura.status}
            </CardDescription>
          </CardHeader>
          {ativa && (
            <CardFooter className="gap-2">
              <Button
                variant="outline"
                size="sm"
                loading={portalMutation.isPending}
                onClick={() => portalMutation.mutate()}
              >
                <CreditCard className="h-4 w-4" /> Gerenciar assinatura
              </Button>
              <Button variant="ghost" size="sm" onClick={() => setCancelando(true)}>
                Cancelar assinatura
              </Button>
            </CardFooter>
          )}
        </Card>
      )}

      {!ativa && plano && (
        <>
          <Separator />
          <div className="max-w-sm">
            <PlanoCard plano={plano} loading={checkoutMutation.isPending} onAssinar={() => checkoutMutation.mutate()} />
          </div>
        </>
      )}

      <ConfirmDialog
        open={cancelando}
        onOpenChange={setCancelando}
        title="Cancelar assinatura?"
        description="Sua empresa perderá acesso aos recursos do plano ao final do período vigente."
        confirmLabel="Cancelar assinatura"
        loading={cancelarMutation.isPending}
        onConfirm={() => cancelarMutation.mutate()}
      />
    </div>
  )
}

function PlanoCard({ plano, loading, onAssinar }: { plano: Plano; loading: boolean; onAssinar: () => void }) {
  return (
    <Card className="border-primary shadow-md">
      <CardHeader>
        <CardTitle>{plano.nome}</CardTitle>
        <CardDescription>
          <span className="text-2xl font-semibold text-foreground">{formatCurrency(plano.precoMensal)}</span>
          <span className="text-sm">/mês</span>
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-2 text-sm">
        <p className="text-muted-foreground">{formatLimite(plano.limiteTanques, 'tanque', 'tanques')}</p>
        <p className="text-muted-foreground">{formatLimite(plano.limiteUsuarios, 'usuário', 'usuários')}</p>
        <Separator className="my-2" />
        <ul className="space-y-1.5">
          {Object.entries(plano.recursos).map(([recurso, incluido]) => (
            <li key={recurso} className="flex items-center gap-2">
              {incluido ? (
                <Check className="h-4 w-4 text-success" />
              ) : (
                <X className="h-4 w-4 text-muted-foreground" />
              )}
              <span className={incluido ? '' : 'text-muted-foreground line-through'}>
                {recursoLabels[recurso] ?? recurso}
              </span>
            </li>
          ))}
        </ul>
      </CardContent>
      <CardFooter>
        <Button className="w-full" loading={loading} onClick={onAssinar}>
          Assinar agora
        </Button>
      </CardFooter>
    </Card>
  )
}

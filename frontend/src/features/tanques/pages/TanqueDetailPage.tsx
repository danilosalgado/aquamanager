import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Droplets, Layers, ThermometerSun } from 'lucide-react'
import {
  ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as ChartTooltip, Legend,
} from 'recharts'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { StatCard } from '@/components/shared/StatCard'
import { tanquesApi } from '../api/tanques-api'
import { qualidadeAguaApi } from '@/features/qualidade-agua/api/qualidade-agua-api'
import { lotesApi } from '@/features/lotes/api/lotes-api'
import { saudeApi, classificacaoConfig } from '@/features/saude/api/saude-api'
import { tipoTanqueLabels, statusTanqueLabels } from '../schemas/tanque-schema'
import { formatDateTime } from '@/lib/utils'

export default function TanqueDetailPage() {
  const { id } = useParams<{ id: string }>()

  const { data: tanque, isLoading } = useQuery({
    queryKey: ['tanques', id],
    queryFn: () => tanquesApi.buscar(id!),
    enabled: !!id,
  })

  const { data: saude } = useQuery({
    queryKey: ['saude', id],
    queryFn: () => saudeApi.atual(id!),
    enabled: !!id,
  })

  const { data: historico } = useQuery({
    queryKey: ['qualidade-agua', id, 'historico'],
    queryFn: () => qualidadeAguaApi.listar({ tanqueId: id, size: 30, sort: 'medidoEm,desc' }),
    enabled: !!id,
  })

  const { data: lotes } = useQuery({
    queryKey: ['lotes', 'tanque', id],
    queryFn: () => lotesApi.listar({ tanqueId: id, status: 'ATIVO', size: 5 }),
    enabled: !!id,
  })

  const chartData = [...(historico?.content ?? [])]
    .reverse()
    .map((r) => ({
      data: new Date(r.medidoEm).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }),
      temperatura: r.temperatura,
      oxigenio: r.oxigenioDissolvido,
      ph: r.ph,
    }))

  if (isLoading || !tanque) {
    return <Skeleton className="h-96 w-full" />
  }

  const classificacao = saude?.classificacao ? classificacaoConfig[saude.classificacao] : null

  return (
    <div>
      <Button variant="ghost" size="sm" className="mb-3" asChild>
        <Link to="/tanques">
          <ArrowLeft className="h-4 w-4" /> Voltar
        </Link>
      </Button>

      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold tracking-tight">{tanque.nome}</h1>
          <p className="text-sm text-muted-foreground">
            {tipoTanqueLabels[tanque.tipo]} · Código {tanque.codigo}
          </p>
        </div>
        <Badge variant={tanque.status === 'ATIVO' ? 'success' : 'secondary'}>
          {statusTanqueLabels[tanque.status]}
        </Badge>
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="Índice de saúde"
          value={saude?.semDadosSuficientes ? 'Sem dados' : `${saude?.score ?? '—'} / 100`}
          icon={Droplets}
          trendLabel={classificacao ? `${classificacao.emoji} ${classificacao.label}` : undefined}
        />
        <StatCard label="Lotes ativos" value={String(lotes?.totalElements ?? 0)} icon={Layers} />
        <StatCard
          label="Volume"
          value={tanque.volumeM3 ? `${tanque.volumeM3} m³` : '—'}
          icon={ThermometerSun}
        />
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Qualidade da água (últimos registros)</CardTitle>
        </CardHeader>
        <CardContent className="h-72">
          {chartData.length === 0 ? (
            <p className="flex h-full items-center justify-center text-sm text-muted-foreground">
              Nenhum registro de qualidade da água ainda.
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="data" fontSize={12} tickLine={false} />
                <YAxis fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Line type="monotone" dataKey="temperatura" name="Temperatura (°C)" stroke="hsl(var(--primary))" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="oxigenio" name="Oxigênio (mg/L)" stroke="hsl(217 91% 60%)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="ph" name="pH" stroke="hsl(38 92% 50%)" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {saude?.detalhes && saude.detalhes.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Observações do índice de saúde</CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="list-inside list-disc space-y-1 text-sm text-muted-foreground">
              {saude.detalhes.map((d, i) => (
                <li key={i}>{d}</li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}

      {tanque.observacoes && (
        <p className="mt-4 text-xs text-muted-foreground">Atualizado em {formatDateTime(new Date())}</p>
      )}
    </div>
  )
}

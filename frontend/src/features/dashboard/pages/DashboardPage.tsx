import { useQuery } from '@tanstack/react-query'
import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as ChartTooltip, Legend,
  AreaChart, Area, LineChart, Line
} from 'recharts'
import {
  Waves, Fish, Scale, TrendingUp, Wallet, Skull, Bell, HeartPulse, Trophy, Package
} from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatCard } from '@/components/shared/StatCard'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { dashboardApi } from '../api/dashboard-api'
import { useAuth } from '@/hooks/use-auth'
import { formatCurrency, formatNumber } from '@/lib/utils'
import { WeatherWidget } from '../components/WeatherWidget'

export default function DashboardPage() {
  const { usuario, hasRole } = useAuth()
  const podeVerFinanceiro = hasRole('ADMINISTRADOR', 'GERENTE', 'CONSULTOR')

  const { data: resumo, isLoading } = useQuery({
    queryKey: ['dashboard', 'resumo'],
    queryFn: dashboardApi.resumo,
  })

  const { data: financeiro } = useQuery({
    queryKey: ['dashboard', 'grafico-financeiro'],
    queryFn: () => dashboardApi.graficoFinanceiro(6),
    enabled: podeVerFinanceiro,
  })

  const { data: saude } = useQuery({
    queryKey: ['dashboard', 'grafico-saude'],
    queryFn: () => dashboardApi.graficoIndiceSaude(30),
  })

  const { data: consumoRacao } = useQuery({
    queryKey: ['dashboard', 'grafico-consumo-racao'],
    queryFn: () => dashboardApi.graficoConsumoRacao(30),
  })

  const { data: crescimento } = useQuery({
    queryKey: ['dashboard', 'grafico-crescimento'],
    queryFn: () => dashboardApi.graficoCrescimento(30),
  })

  const { data: mortalidade } = useQuery({
    queryKey: ['dashboard', 'grafico-mortalidade'],
    queryFn: () => dashboardApi.graficoMortalidade(30),
  })

  const { data: qualidadeAgua } = useQuery({
    queryKey: ['dashboard', 'grafico-qualidade-agua'],
    queryFn: () => dashboardApi.graficoQualidadeAgua(30),
  })

  const { data: biomassa } = useQuery({
    queryKey: ['dashboard', 'grafico-biomassa'],
    queryFn: () => dashboardApi.graficoBiomassa(90),
  })

  const { data: producaoTanques } = useQuery({
    queryKey: ['dashboard', 'producao-por-tanque'],
    queryFn: dashboardApi.producaoPorTanque,
  })

  const primeiroNome = usuario?.nome?.split(' ')[0]
  const totalConsumoRacao = consumoRacao?.reduce((acc, curr) => acc + curr.valor, 0) || 0

  const topTanques = producaoTanques
    ? [...producaoTanques].sort((a, b) => b.biomassaKg - a.biomassaKg).slice(0, 5)
    : []

  const formatDate = (dateStr: string) => new Date(dateStr).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })

  return (
    <div className="space-y-6">
      <PageHeader title={`Olá, ${primeiroNome} 👋`} description="Aqui está o panorama da sua produção hoje." />

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <WeatherWidget />
        <StatCard label="Tanques" value={formatNumber(resumo?.quantidadeTanques)} icon={Waves} loading={isLoading} />
        <StatCard label="Peixes em produção" value={formatNumber(resumo?.quantidadePeixes)} icon={Fish} loading={isLoading} />
        <StatCard label="Biomassa total" value={`${formatNumber(resumo?.biomassaTotalKg, 1)} kg`} icon={Scale} loading={isLoading} />
        <StatCard label="Peso médio" value={`${formatNumber(resumo?.pesoMedioG, 0)} g`} icon={TrendingUp} loading={isLoading} />
        
        <StatCard label="Consumo Ração (30d)" value={`${formatNumber(totalConsumoRacao, 1)} kg`} icon={Package} loading={isLoading} />
        
        {podeVerFinanceiro && (
          <>
            <StatCard label="Receitas (30d)" value={formatCurrency(resumo?.receita30Dias)} icon={Wallet} loading={isLoading} tone="success" />
            <StatCard label="Despesas (30d)" value={formatCurrency(resumo?.despesa30Dias)} icon={Wallet} loading={isLoading} tone="destructive" />
            <StatCard
              label="Lucro (30 dias)"
              value={formatCurrency(resumo?.lucro30Dias)}
              icon={Wallet}
              loading={isLoading}
              tone={resumo && resumo.lucro30Dias < 0 ? 'destructive' : 'success'}
            />
          </>
        )}
        
        <StatCard
          label="Conversão alimentar (FCR)"
          value={resumo?.conversaoAlimentarMedia ? formatNumber(resumo.conversaoAlimentarMedia, 2) : '—'}
          icon={TrendingUp}
          loading={isLoading}
        />
        <StatCard
          label="Mortalidade (30 dias)"
          value={formatNumber(resumo?.mortalidade30Dias)}
          icon={Skull}
          loading={isLoading}
          tone={resumo && resumo.mortalidade30Dias > 0 ? 'warning' : 'default'}
        />
        <StatCard
          label="Alertas não lidos"
          value={formatNumber(resumo?.alertasNaoLidos)}
          icon={Bell}
          loading={isLoading}
          tone={resumo && resumo.alertasNaoLidos > 0 ? 'destructive' : 'default'}
        />
        <StatCard
          label="Índice de saúde médio"
          value={resumo?.indiceSaudeMedio != null ? `${formatNumber(resumo.indiceSaudeMedio, 0)} / 100` : '—'}
          icon={HeartPulse}
          loading={isLoading}
        />
      </div>

      {/* Gráficos Principais */}
      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Evolução da Biomassa (90 dias)</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={(biomassa ?? []).map(d => ({ data: formatDate(d.data), valor: d.valor }))}>
                <defs>
                  <linearGradient id="biomassaGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="data" fontSize={12} tickLine={false} />
                <YAxis fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Area type="monotone" dataKey="valor" name="Biomassa (kg)" stroke="hsl(var(--primary))" fill="url(#biomassaGrad)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Crescimento: Peso Médio (30 dias)</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={(crescimento ?? []).map(d => ({ data: formatDate(d.data), valor: d.valor }))}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="data" fontSize={12} tickLine={false} />
                <YAxis fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Line type="monotone" dataKey="valor" name="Peso Médio (g)" stroke="hsl(217 91% 60%)" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Consumo e Mortalidade */}
      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Consumo de Ração Diário (30 dias)</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={(consumoRacao ?? []).map(d => ({ data: formatDate(d.data), valor: d.valor }))}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="data" fontSize={12} tickLine={false} />
                <YAxis fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Bar dataKey="valor" name="Ração (kg)" fill="hsl(var(--success))" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Mortalidade Diária (30 dias)</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={(mortalidade ?? []).map(d => ({ data: formatDate(d.data), valor: d.valor }))}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="data" fontSize={12} tickLine={false} />
                <YAxis fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Bar dataKey="valor" name="Mortos" fill="hsl(var(--warning))" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Qualidade da Água e Saúde */}
      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Qualidade da Água Média (30 dias)</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={(qualidadeAgua ?? []).map(d => ({ ...d, data: formatDate(d.data) }))}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="data" fontSize={12} tickLine={false} />
                <YAxis fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Line type="monotone" dataKey="temperatura" name="Temperatura (°C)" stroke="hsl(var(--primary))" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="oxigenioDissolvido" name="Oxigênio (mg/L)" stroke="hsl(217 91% 60%)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="ph" name="pH" stroke="hsl(38 92% 50%)" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Índice de saúde médio (30 dias)</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={(saude ?? []).map(s => ({ data: formatDate(s.data), score: s.scoreMedio }))}>
                <defs>
                  <linearGradient id="saudeGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="hsl(var(--success))" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="hsl(var(--success))" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="data" fontSize={12} tickLine={false} />
                <YAxis domain={[0, 100]} fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Area type="monotone" dataKey="score" name="Score médio" stroke="hsl(var(--success))" fill="url(#saudeGradient)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Comparativos e Rankings */}
      <div className="grid gap-4 lg:grid-cols-[2fr_1fr]">
        {podeVerFinanceiro ? (
          <Card>
            <CardHeader>
              <CardTitle>Receita x Despesa (últimos 6 meses)</CardTitle>
            </CardHeader>
            <CardContent className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={financeiro ?? []}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                  <XAxis dataKey="rotulo" fontSize={12} tickLine={false} />
                  <YAxis fontSize={12} tickLine={false} tickFormatter={(v) => formatCurrency(v)} width={90} />
                  <ChartTooltip formatter={(v: number) => formatCurrency(v)} contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                  <Legend wrapperStyle={{ fontSize: 12 }} />
                  <Bar dataKey="valorReceita" name="Receita" fill="hsl(var(--success))" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="valorDespesa" name="Despesa" fill="hsl(var(--destructive))" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardHeader>
              <CardTitle>Produção por Tanque</CardTitle>
            </CardHeader>
            <CardContent className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={producaoTanques ?? []}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                  <XAxis dataKey="tanqueNome" fontSize={12} tickLine={false} />
                  <YAxis fontSize={12} tickLine={false} />
                  <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                  <Bar dataKey="biomassaKg" name="Biomassa (kg)" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Trophy className="h-5 w-5 text-warning" />
              Ranking: Top 5 Tanques
            </CardTitle>
          </CardHeader>
          <CardContent>
            {topTanques.length === 0 ? (
              <p className="text-sm text-muted-foreground">Nenhum dado disponível.</p>
            ) : (
              <div className="space-y-4">
                {topTanques.map((t, i) => (
                  <div key={t.tanqueNome} className="flex items-center justify-between border-b pb-2 last:border-0 last:pb-0">
                    <div className="flex items-center gap-3">
                      <span className="flex h-6 w-6 items-center justify-center rounded-full bg-secondary text-xs font-bold text-secondary-foreground">
                        {i + 1}
                      </span>
                      <div>
                        <p className="text-sm font-medium">{t.tanqueNome}</p>
                        <p className="text-xs text-muted-foreground">Saúde: {t.indiceSaude != null ? t.indiceSaude : '—'}/100</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-bold text-primary">{formatNumber(t.biomassaKg, 1)} kg</p>
                      <p className="text-xs text-muted-foreground">{formatNumber(t.quantidadePeixes)} peixes</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
      
      {podeVerFinanceiro && (
        <Card>
          <CardHeader>
            <CardTitle>Produção por Tanque</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={producaoTanques ?? []}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="tanqueNome" fontSize={12} tickLine={false} />
                <YAxis fontSize={12} tickLine={false} />
                <ChartTooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} />
                <Bar dataKey="biomassaKg" name="Biomassa (kg)" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

    </div>
  )
}

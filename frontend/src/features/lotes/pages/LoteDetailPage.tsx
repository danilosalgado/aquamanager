import { useMemo, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  ArrowLeft, Fish, Scale, TrendingUp, Target, Zap, Sparkles, AlertTriangle, CheckCircle2,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { StatCard } from '@/components/shared/StatCard'
import { lotesApi, type ProjecaoPeso } from '../api/lotes-api'
import { statusLoteLabels } from '../schemas/lote-schema'
import { formatNumber, formatCurrency, formatDate, cn } from '@/lib/utils'

const confiabilidadeConfig: Record<string, { label: string; tone: 'success' | 'warning' | 'destructive' }> = {
  ALTA: { label: 'Alta confiança', tone: 'success' },
  MEDIA: { label: 'Confiança média', tone: 'warning' },
  BAIXA: { label: 'Confiança baixa', tone: 'destructive' },
}

interface Cenario {
  projecao: ProjecaoPeso
  ganhoBiomassaKg: number
  consumoRacaoKg: number
  custoRacao: number
  faturamento: number
  lucro: number
  lucroPorDia: number | null
}

export default function LoteDetailPage() {
  const { id } = useParams<{ id: string }>()

  const [precoKg, setPrecoKg] = useState(12.5)
  const [custoRacaoKg, setCustoRacaoKg] = useState(4.2)
  const [conversaoAlimentar, setConversaoAlimentar] = useState(1.6)

  const { data: lote, isLoading: carregandoLote } = useQuery({
    queryKey: ['lotes', id],
    queryFn: () => lotesApi.buscar(id!),
    enabled: !!id,
  })

  const { data: potencial, isLoading: carregandoPotencial } = useQuery({
    queryKey: ['lotes', id, 'crescimento-potencial'],
    queryFn: () => lotesApi.crescimentoPotencial(id!),
    enabled: !!id,
  })

  const cenarios = useMemo<Cenario[]>(() => {
    if (!potencial || !lote) return []
    return potencial.projecoes.map((projecao) => {
      const ganhoPesoG = Math.max(0, projecao.pesoAlvoG - potencial.pesoAtualG)
      const ganhoBiomassaKg = (ganhoPesoG / 1000) * lote.quantidadeAtual
      const consumoRacaoKg = ganhoBiomassaKg * conversaoAlimentar
      const custoRacao = consumoRacaoKg * custoRacaoKg
      const faturamento = (projecao.pesoAlvoG / 1000) * lote.quantidadeAtual * precoKg
      const lucro = faturamento - custoRacao
      const lucroPorDia = projecao.diasRestantes && projecao.diasRestantes > 0 ? lucro / projecao.diasRestantes : null
      return { projecao, ganhoBiomassaKg, consumoRacaoKg, custoRacao, faturamento, lucro, lucroPorDia }
    })
  }, [potencial, lote, precoKg, custoRacaoKg, conversaoAlimentar])

  const alcancaveis = cenarios.filter((c) => c.projecao.dataPrevista || c.projecao.jaAtingido)
  const maisRapido = alcancaveis[0]
  // "Maximizar lucro" compara o valor total em reais, não a taxa por dia — esperar mais
  // pra vender um peixe mais pesado normalmente rende mais dinheiro no total, mesmo que
  // o lucro por dia de espera seja menor (essa taxa continua na tabela pra quem quiser
  // considerar o custo de oportunidade de segurar o tanque por mais tempo).
  const maisLucrativo = alcancaveis.reduce<Cenario | null>((melhor, atual) => {
    if (!melhor || atual.lucro > melhor.lucro) return atual
    return melhor
  }, null)

  if (carregandoLote || !lote) {
    return <Skeleton className="h-96 w-full" />
  }

  const confiabilidade = potencial ? confiabilidadeConfig[potencial.confiabilidade] : null

  return (
    <div>
      <Button variant="ghost" size="sm" className="mb-3" asChild>
        <Link to="/lotes">
          <ArrowLeft className="h-4 w-4" /> Voltar
        </Link>
      </Button>

      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold tracking-tight">{lote.especieNome} · {lote.tanqueNome}</h1>
          <p className="text-sm text-muted-foreground">
            {formatNumber(lote.quantidadeAtual)} peixes · Povoado em {formatDate(lote.dataCompra)}
          </p>
        </div>
        <Badge variant={lote.status === 'ATIVO' ? 'success' : 'secondary'}>
          {statusLoteLabels[lote.status]}
        </Badge>
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Peso médio atual" value={`${formatNumber(lote.pesoAtualG, 0)} g`} icon={Scale} />
        <StatCard
          label="Taxa de crescimento"
          value={potencial?.taxaCrescimentoGDia != null ? `${formatNumber(potencial.taxaCrescimentoGDia, 2)} g/dia` : '—'}
          icon={TrendingUp}
          loading={carregandoPotencial}
        />
        <StatCard
          label="Confiabilidade da estimativa"
          value={confiabilidade?.label ?? '—'}
          icon={confiabilidade?.tone === 'success' ? CheckCircle2 : AlertTriangle}
          tone={confiabilidade?.tone}
          trendLabel={potencial ? `Baseado em ${potencial.pesagensConsideradas} pesagem(ns)` : undefined}
          trend="neutral"
          loading={carregandoPotencial}
        />
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Target className="h-4 w-4 text-primary" /> Crescimento Potencial
          </CardTitle>
          <CardDescription>
            Estimativa de quanto tempo falta para este lote atingir cada peso-alvo, com base no ritmo de
            crescimento medido nas últimas pesagens.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {carregandoPotencial ? (
            <div className="space-y-2">
              {Array.from({ length: 3 }).map((_, i) => (
                <Skeleton key={i} className="h-10 w-full" />
              ))}
            </div>
          ) : !potencial || potencial.taxaCrescimentoGDia == null ? (
            <p className="text-sm text-muted-foreground">
              Ainda não há dados suficientes para estimar o crescimento deste lote — registre ao menos uma
              pesagem em Crescimento.
            </p>
          ) : (
            <div className="flex flex-wrap gap-3">
              {potencial.projecoes.map((p) => (
                <div
                  key={p.rotulo}
                  className={cn(
                    'flex min-w-[160px] flex-1 basis-40 flex-col rounded-xl border p-4',
                    p.jaAtingido ? 'border-success/40 bg-success/5' : 'border-border bg-muted/20',
                  )}
                >
                  <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{p.rotulo}</p>
                  {p.jaAtingido ? (
                    <p className="mt-1 text-sm font-semibold text-success">Já atingido</p>
                  ) : p.diasRestantes != null ? (
                    <>
                      <p className="mt-1 text-2xl font-bold tracking-tight">{p.diasRestantes} dias</p>
                      <p className="text-xs text-muted-foreground">até {formatDate(p.dataPrevista)}</p>
                    </>
                  ) : (
                    <p className="mt-1 text-sm text-muted-foreground">Não é possível estimar</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {potencial && potencial.taxaCrescimentoGDia != null && cenarios.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Sparkles className="h-4 w-4 text-primary" /> Vender rápido ou maximizar lucro?
            </CardTitle>
            <CardDescription>
              Ajuste os valores abaixo (você pode usar suas próprias estimativas de preço e custo) para ver qual
              peso-alvo entrega mais dinheiro por dia de espera.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1.5">
                <Label>Preço de venda (R$/kg)</Label>
                <Input type="number" step="0.1" value={precoKg} onChange={(e) => setPrecoKg(Number(e.target.value))} />
              </div>
              <div className="space-y-1.5">
                <Label>Custo da ração (R$/kg)</Label>
                <Input type="number" step="0.1" value={custoRacaoKg} onChange={(e) => setCustoRacaoKg(Number(e.target.value))} />
              </div>
              <div className="space-y-1.5">
                <Label>Conversão alimentar (FCA)</Label>
                <Input type="number" step="0.1" value={conversaoAlimentar} onChange={(e) => setConversaoAlimentar(Number(e.target.value))} />
              </div>
            </div>

            <Separator />

            <div className="grid gap-4 sm:grid-cols-2">
              {maisRapido && (
                <div className="rounded-xl border border-primary/30 bg-primary/5 p-4">
                  <p className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-primary">
                    <Zap className="h-3.5 w-3.5" /> Vender o quanto antes
                  </p>
                  <p className="mt-2 text-lg font-bold">{maisRapido.projecao.rotulo}</p>
                  <p className="text-sm text-muted-foreground">
                    {maisRapido.projecao.jaAtingido
                      ? 'Já pode vender agora'
                      : `Em ${maisRapido.projecao.diasRestantes} dias (${formatDate(maisRapido.projecao.dataPrevista)})`}
                  </p>
                  <p className="mt-2 text-sm">
                    Lucro estimado: <span className="font-semibold text-success">{formatCurrency(maisRapido.lucro)}</span>
                  </p>
                </div>
              )}

              {maisLucrativo && (
                <div className="rounded-xl border border-success/30 bg-success/5 p-4">
                  <p className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-success">
                    <TrendingUp className="h-3.5 w-3.5" /> Maximizar lucro
                  </p>
                  <p className="mt-2 text-lg font-bold">{maisLucrativo.projecao.rotulo}</p>
                  <p className="text-sm text-muted-foreground">
                    Em {maisLucrativo.projecao.diasRestantes} dias ({formatDate(maisLucrativo.projecao.dataPrevista)})
                  </p>
                  <p className="mt-2 text-sm">
                    Lucro estimado: <span className="font-semibold text-success">{formatCurrency(maisLucrativo.lucro)}</span>
                    {maisLucrativo.lucroPorDia != null && (
                      <span className="text-muted-foreground"> · {formatCurrency(maisLucrativo.lucroPorDia)}/dia</span>
                    )}
                  </p>
                </div>
              )}
            </div>

            <div className="rounded-xl border border-border">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-left text-xs uppercase tracking-wide text-muted-foreground">
                    <th className="px-4 py-2 font-medium">Meta</th>
                    <th className="px-4 py-2 font-medium">Prazo</th>
                    <th className="px-4 py-2 font-medium">Custo ração</th>
                    <th className="px-4 py-2 font-medium">Faturamento</th>
                    <th className="px-4 py-2 font-medium">Lucro</th>
                    <th className="px-4 py-2 font-medium">Lucro/dia</th>
                  </tr>
                </thead>
                <tbody>
                  {cenarios.map((c) => (
                    <tr key={c.projecao.rotulo} className="border-b border-border/60 last:border-0">
                      <td className="px-4 py-2 font-medium">
                        <div className="flex items-center gap-1.5">
                          <Fish className="h-3.5 w-3.5 text-muted-foreground" /> {c.projecao.rotulo}
                        </div>
                      </td>
                      <td className="px-4 py-2 text-muted-foreground">
                        {c.projecao.jaAtingido ? 'Agora' : c.projecao.diasRestantes != null ? `${c.projecao.diasRestantes} dias` : '—'}
                      </td>
                      <td className="px-4 py-2 text-muted-foreground">{formatCurrency(c.custoRacao)}</td>
                      <td className="px-4 py-2 text-muted-foreground">{formatCurrency(c.faturamento)}</td>
                      <td className="px-4 py-2 font-medium text-success">{formatCurrency(c.lucro)}</td>
                      <td className="px-4 py-2 text-muted-foreground">
                        {c.lucroPorDia != null ? formatCurrency(c.lucroPorDia) : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="text-xs text-muted-foreground">
              *Cálculo simplificado (considera só o custo de ração adicional) — os preços acima são estimativas
              suas, ajuste conforme o mercado.
            </p>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

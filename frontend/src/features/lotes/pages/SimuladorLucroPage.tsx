import { useState } from 'react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Calculator, TrendingUp } from 'lucide-react'
import { formatCurrency, formatNumber } from '@/lib/utils'

export default function SimuladorLucroPage() {
  const [pesoAtual, setPesoAtual] = useState(50) // gramas
  const [pesoAlvo, setPesoAlvo] = useState(800) // gramas
  const [quantidade, setQuantidade] = useState(10000)
  const [precoKg, setPrecoKg] = useState(12.50)
  const [custoRacaoKg, setCustoRacaoKg] = useState(4.20)
  const [conversaoAlimentar, setConversaoAlimentar] = useState(1.6)

  const calcularSimulacao = () => {
    const ganhoPesoG = pesoAlvo - pesoAtual
    const ganhoPesoKg = ganhoPesoG / 1000
    const ganhoTotalKg = ganhoPesoKg * quantidade
    const consumoRacaoKg = ganhoTotalKg * conversaoAlimentar
    
    const custoRacao = consumoRacaoKg * custoRacaoKg
    const faturamentoEstimado = (pesoAlvo / 1000) * quantidade * precoKg
    const lucroBrutoEstimado = faturamentoEstimado - custoRacao // simplificado

    return {
      ganhoTotalKg,
      consumoRacaoKg,
      custoRacao,
      faturamentoEstimado,
      lucroBrutoEstimado
    }
  }

  const result = calcularSimulacao()

  return (
    <div className="space-y-6">
      <PageHeader 
        title="Simulador de Lucro" 
        description="Estime os custos de ração e o lucro esperado para engordar seus peixes até o peso de abate."
      />

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calculator className="h-5 w-5 text-primary" />
              Dados do Lote
            </CardTitle>
            <CardDescription>Preencha os dados atuais e a sua meta.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Quantidade de Peixes</Label>
                <Input type="number" value={quantidade} onChange={e => setQuantidade(Number(e.target.value))} />
              </div>
              <div className="space-y-2">
                <Label>Conversão Alimentar (FCA)</Label>
                <Input type="number" step="0.1" value={conversaoAlimentar} onChange={e => setConversaoAlimentar(Number(e.target.value))} />
              </div>
              <div className="space-y-2">
                <Label>Peso Médio Atual (g)</Label>
                <Input type="number" value={pesoAtual} onChange={e => setPesoAtual(Number(e.target.value))} />
              </div>
              <div className="space-y-2">
                <Label>Peso Alvo (Abate) (g)</Label>
                <Input type="number" value={pesoAlvo} onChange={e => setPesoAlvo(Number(e.target.value))} />
              </div>
              <div className="space-y-2">
                <Label>Custo Ração (R$/kg)</Label>
                <Input type="number" step="0.1" value={custoRacaoKg} onChange={e => setCustoRacaoKg(Number(e.target.value))} />
              </div>
              <div className="space-y-2">
                <Label>Preço de Venda (R$/kg)</Label>
                <Input type="number" step="0.1" value={precoKg} onChange={e => setPrecoKg(Number(e.target.value))} />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-primary/5 border-primary/20">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-primary" />
              Resultado da Simulação
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex justify-between items-center pb-2 border-b border-border">
              <span className="text-muted-foreground">Ganho de Biomassa:</span>
              <span className="font-semibold">{formatNumber(result.ganhoTotalKg)} kg</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-border">
              <span className="text-muted-foreground">Consumo de Ração Estimado:</span>
              <span className="font-semibold">{formatNumber(result.consumoRacaoKg)} kg</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-border text-rose-500">
              <span>Custo Adicional (Ração):</span>
              <span className="font-semibold">{formatCurrency(result.custoRacao)}</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-border text-emerald-500">
              <span>Faturamento Bruto (Venda):</span>
              <span className="font-semibold">{formatCurrency(result.faturamentoEstimado)}</span>
            </div>
            <div className="pt-4 flex flex-col items-center justify-center p-4 bg-background rounded-xl border border-border shadow-sm">
              <span className="text-sm font-medium text-muted-foreground uppercase tracking-wider mb-1">Margem / Lucro Estimado</span>
              <span className="text-4xl font-bold text-emerald-600 dark:text-emerald-400">
                {formatCurrency(result.lucroBrutoEstimado)}
              </span>
              <p className="text-xs text-muted-foreground mt-2 text-center">
                *Cálculo simplificado considerando apenas o custo com ração para engordar do peso atual até o alvo.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

import { useState } from 'react'
import { FileText, FileSpreadsheet, FileBarChart, Wallet, Skull, Boxes, Activity } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { relatoriosApi } from '../api/relatorios-api'
import { toast } from 'sonner'
import { format, subMonths } from 'date-fns'

const relatoriosDisponiveis = [
  {
    id: 'financeiro',
    titulo: 'Financeiro',
    descricao: 'Receitas, despesas e fluxo de caixa detalhado.',
    icone: Wallet,
    color: 'text-emerald-500',
    bg: 'bg-emerald-500/10',
    requerData: true,
  },
  {
    id: 'producao',
    titulo: 'Produção',
    descricao: 'Histórico de lotes, biomassa e conversão alimentar.',
    icone: FileBarChart,
    color: 'text-blue-500',
    bg: 'bg-blue-500/10',
    requerData: true,
  },
  {
    id: 'mortalidade',
    titulo: 'Mortalidade',
    descricao: 'Registros de perdas, motivos e lotes afetados.',
    icone: Skull,
    color: 'text-rose-500',
    bg: 'bg-rose-500/10',
    requerData: true,
  },
  {
    id: 'estoque',
    titulo: 'Estoque Atual',
    descricao: 'Posição atual de insumos, rações e equipamentos.',
    icone: Boxes,
    color: 'text-amber-500',
    bg: 'bg-amber-500/10',
    requerData: false,
  },
] as const

export default function RelatoriosPage() {
  const [inicio, setInicio] = useState(format(subMonths(new Date(), 1), "yyyy-MM-dd'T'HH:mm"))
  const [fim, setFim] = useState(format(new Date(), "yyyy-MM-dd'T'HH:mm"))
  const [loading, setLoading] = useState<string | null>(null)

  const handleDownload = async (id: any, formato: 'pdf' | 'excel', requerData: boolean) => {
    try {
      setLoading(`${id}-${formato}`)
      const dataInicio = requerData ? new Date(inicio) : undefined
      const dataFim = requerData ? new Date(fim) : undefined
      await relatoriosApi.baixar(id, formato, dataInicio, dataFim)
      toast.success('Download iniciado')
    } catch (error) {
      toast.error('Erro ao gerar relatório. Tente novamente.')
    } finally {
      setLoading(null)
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Relatórios"
        description="Exporte dados da sua produção para análise em PDF ou planilhas Excel."
      />

      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Filtro de Período</CardTitle>
          <CardDescription>O período selecionado será aplicado aos relatórios que requerem datas.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-xl">
            <div className="space-y-2">
              <Label htmlFor="inicio">Data Inicial</Label>
              <Input
                id="inicio"
                type="datetime-local"
                value={inicio}
                onChange={(e) => setInicio(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="fim">Data Final</Label>
              <Input
                id="fim"
                type="datetime-local"
                value={fim}
                onChange={(e) => setFim(e.target.value)}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {relatoriosDisponiveis.map((relatorio) => (
          <Card key={relatorio.id} className="flex flex-col">
            <CardHeader>
              <div className="flex items-center gap-3">
                <div className={`p-2 rounded-lg ${relatorio.bg}`}>
                  <relatorio.icone className={`h-6 w-6 ${relatorio.color}`} />
                </div>
                <div>
                  <CardTitle className="text-lg">{relatorio.titulo}</CardTitle>
                </div>
              </div>
            </CardHeader>
            <CardContent className="flex-1">
              <p className="text-sm text-muted-foreground">{relatorio.descricao}</p>
            </CardContent>
            <CardFooter className="flex gap-2 pt-4 border-t border-border mt-auto">
              <Button
                variant="outline"
                className="flex-1"
                disabled={loading !== null}
                onClick={() => handleDownload(relatorio.id, 'pdf', relatorio.requerData)}
              >
                {loading === `${relatorio.id}-pdf` ? (
                  <Activity className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <FileText className="h-4 w-4 mr-2" />
                )}
                PDF
              </Button>
              <Button
                variant="outline"
                className="flex-1"
                disabled={loading !== null}
                onClick={() => handleDownload(relatorio.id, 'excel', relatorio.requerData)}
              >
                {loading === `${relatorio.id}-excel` ? (
                  <Activity className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <FileSpreadsheet className="h-4 w-4 mr-2" />
                )}
                Excel
              </Button>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  )
}

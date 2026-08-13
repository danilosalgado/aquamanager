import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Target, ChevronRight, TrendingUp } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import { lotesApi, type CrescimentoPotencial } from '../api/lotes-api'
import { formatNumber, formatDate } from '@/lib/utils'

const confiabilidadeConfig: Record<string, { label: string; tone: 'success' | 'warning' | 'destructive' }> = {
  ALTA: { label: 'Alta', tone: 'success' },
  MEDIA: { label: 'Média', tone: 'warning' },
  BAIXA: { label: 'Baixa', tone: 'destructive' },
}

function proximaMeta(potencial: CrescimentoPotencial) {
  return potencial.projecoes.find((p) => !p.jaAtingido && p.diasRestantes != null) ?? null
}

export default function CrescimentoPotencialOverviewPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['lotes', 'crescimento-potencial'],
    queryFn: () => lotesApi.crescimentoPotencialTodosAtivos(),
  })

  return (
    <div>
      <PageHeader
        title="Crescimento Potencial"
        description="Quanto tempo falta para cada lote ativo atingir os próximos pesos-alvo, com base no ritmo de crescimento real medido."
      />

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-14 w-full" />
          ))}
        </div>
      ) : !data?.length ? (
        <EmptyState
          icon={Target}
          title="Nenhum lote ativo"
          description="Cadastre lotes e registre pesagens em Biometria para ver as projeções aqui."
        />
      ) : (
        <div className="rounded-xl border border-border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Lote</TableHead>
                <TableHead>Peso atual</TableHead>
                <TableHead>Taxa de crescimento</TableHead>
                <TableHead>Confiabilidade</TableHead>
                <TableHead>Próxima meta</TableHead>
                <TableHead className="w-10" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.map((potencial) => {
                const confiabilidade = confiabilidadeConfig[potencial.confiabilidade]
                const meta = proximaMeta(potencial)
                return (
                  <TableRow key={potencial.loteId} className="cursor-pointer">
                    <TableCell className="font-medium">
                      <Link to={`/lotes/${potencial.loteId}`} className="hover:text-primary hover:underline">
                        {potencial.especieNome} · {potencial.tanqueNome}
                      </Link>
                    </TableCell>
                    <TableCell>{formatNumber(potencial.pesoAtualG, 0)} g</TableCell>
                    <TableCell className="text-muted-foreground">
                      {potencial.taxaCrescimentoGDia != null ? (
                        <span className="inline-flex items-center gap-1">
                          <TrendingUp className="h-3.5 w-3.5" /> {formatNumber(potencial.taxaCrescimentoGDia, 2)} g/dia
                        </span>
                      ) : (
                        '—'
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge variant={confiabilidade?.tone ?? 'secondary'}>{confiabilidade?.label ?? '—'}</Badge>
                    </TableCell>
                    <TableCell>
                      {meta ? (
                        <span>
                          <span className="font-medium">{meta.rotulo}</span>{' '}
                          <span className="text-muted-foreground">
                            em {meta.diasRestantes} dias ({formatDate(meta.dataPrevista)})
                          </span>
                        </span>
                      ) : (
                        <span className="text-muted-foreground">Não é possível estimar</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <Link to={`/lotes/${potencial.loteId}`}>
                        <ChevronRight className="h-4 w-4 text-muted-foreground" />
                      </Link>
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  )
}

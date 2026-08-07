import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, ChevronLeft, ChevronRight, CheckCircle2, Circle } from 'lucide-react'
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isToday, addMonths, subMonths } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import { PageHeader } from '@/components/shared/PageHeader'
import { Button } from '@/components/ui/button'
import { agendaApi, type Evento } from '../api/agenda-api'
import { EventoFormDialog } from '../components/EventoFormDialog'
import { tipoEventoColors } from '../schemas/agenda-schema'
import { cn } from '@/lib/utils'

export default function AgendaPage() {
  const [currentDate, setCurrentDate] = useState(new Date())
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Evento | null>(null)
  
  const queryClient = useQueryClient()
  
  const monthStart = startOfMonth(currentDate)
  const monthEnd = endOfMonth(currentDate)
  const days = eachDayOfInterval({ start: monthStart, end: monthEnd })

  const { data: eventos } = useQuery({
    queryKey: ['agenda', monthStart.toISOString()],
    queryFn: () => agendaApi.listarPorPeriodo(monthStart.toISOString(), monthEnd.toISOString()),
  })

  const toggleMutation = useMutation({
    mutationFn: (id: string) => agendaApi.alternarConcluido(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['agenda'] })
  })

  const getEventosByDate = (date: Date) => {
    if (!eventos) return []
    const dateStr = format(date, 'yyyy-MM-dd')
    return eventos.filter(e => e.dataInicio.startsWith(dateStr))
  }

  const nextMonth = () => setCurrentDate(addMonths(currentDate, 1))
  const prevMonth = () => setCurrentDate(subMonths(currentDate, 1))

  const { data: isGoogleConnected } = useQuery({
    queryKey: ['googleStatus'],
    queryFn: agendaApi.googleStatus,
  })

  const connectGoogle = async () => {
    try {
      const url = await agendaApi.googleAuthUrl()
      window.location.href = url
    } catch (e) {
      console.error(e)
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Agenda e Lembretes"
        description="Gerencie atividades e manutenções da sua produção."
        actions={
          <div className="flex gap-2">
            {!isGoogleConnected ? (
              <Button variant="outline" onClick={connectGoogle}>
                Conectar Google Calendar
              </Button>
            ) : (
              <Button variant="outline" disabled className="text-success border-success">
                Google Calendar Conectado
              </Button>
            )}
            <Button onClick={() => { setEditing(null); setFormOpen(true) }}>
              <Plus className="h-4 w-4 mr-2" /> Novo Evento
            </Button>
          </div>
        }
      />

      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold capitalize">
          {format(currentDate, 'MMMM yyyy', { locale: ptBR })}
        </h2>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="icon" onClick={prevMonth}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <Button variant="outline" onClick={() => setCurrentDate(new Date())}>
            Hoje
          </Button>
          <Button variant="outline" size="icon" onClick={nextMonth}>
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-7 gap-px rounded-xl bg-border overflow-hidden">
        {['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'].map(day => (
          <div key={day} className="bg-card text-center py-2 text-sm font-semibold text-muted-foreground">
            {day}
          </div>
        ))}
        {Array.from({ length: monthStart.getDay() }).map((_, i) => (
          <div key={`empty-${i}`} className="bg-card/50 min-h-[120px]" />
        ))}
        {days.map(day => {
          const dayEventos = getEventosByDate(day)
          return (
            <div key={day.toISOString()} className={cn("bg-card p-2 min-h-[120px] transition-colors hover:bg-accent/50", isToday(day) && "bg-primary/5")}>
              <div className={cn("text-right text-sm font-medium mb-1", isToday(day) && "text-primary")}>
                {format(day, 'd')}
              </div>
              <div className="space-y-1">
                {dayEventos.map(evento => (
                  <div
                    key={evento.id}
                    onClick={() => { setEditing(evento); setFormOpen(true) }}
                    className={cn(
                      "text-xs px-1.5 py-1 rounded-md cursor-pointer truncate flex items-center justify-between group",
                      tipoEventoColors[evento.tipo],
                      evento.concluido && "opacity-50 line-through"
                    )}
                  >
                    <span className="truncate" title={evento.titulo}>{evento.titulo}</span>
                    <button 
                      onClick={(e) => { e.stopPropagation(); toggleMutation.mutate(evento.id) }}
                      className="opacity-0 group-hover:opacity-100 transition-opacity ml-1 flex-shrink-0"
                    >
                      {evento.concluido ? <CheckCircle2 className="h-3 w-3" /> : <Circle className="h-3 w-3" />}
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )
        })}
      </div>

      {formOpen && (
        <EventoFormDialog open={formOpen} onOpenChange={setFormOpen} registro={editing} />
      )}
    </div>
  )
}

import { z } from 'zod'

export const TipoEventoSchema = z.enum([
  'ALIMENTACAO',
  'PESAGEM',
  'LIMPEZA',
  'TROCA_AGUA',
  'VACINACAO',
  'COLETA',
  'VENDA',
  'OUTROS',
])

export type TipoEvento = z.infer<typeof TipoEventoSchema>

export const eventoSchema = z.object({
  tipo: TipoEventoSchema,
  titulo: z.string().min(2, 'O título é obrigatório').max(150),
  descricao: z.string().optional().or(z.literal('')),
  dataInicio: z.string().min(1, 'A data de início é obrigatória'),
  dataFim: z.string().optional().or(z.literal('')),
  concluido: z.boolean().default(false),
})

export type EventoFormValues = z.infer<typeof eventoSchema>

export const tipoEventoLabels: Record<TipoEvento, string> = {
  ALIMENTACAO: 'Alimentação',
  PESAGEM: 'Pesagem',
  LIMPEZA: 'Limpeza',
  TROCA_AGUA: 'Troca de Água',
  VACINACAO: 'Vacinação',
  COLETA: 'Coleta',
  VENDA: 'Venda',
  OUTROS: 'Outros',
}

export const tipoEventoColors: Record<TipoEvento, string> = {
  ALIMENTACAO: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400',
  PESAGEM: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400',
  LIMPEZA: 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900/30 dark:text-cyan-400',
  TROCA_AGUA: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900/30 dark:text-indigo-400',
  VACINACAO: 'bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-400',
  COLETA: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400',
  VENDA: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400',
  OUTROS: 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-400',
}

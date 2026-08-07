import { z } from 'zod'

export const alimentacaoSchema = z.object({
  loteId: z.string().uuid('Selecione o lote.'),
  tipoRacao: z.string().min(1, 'Informe o tipo de ração.').max(100),
  fornecedor: z.string().optional(),
  quantidadeKg: z.coerce.number({ invalid_type_error: 'Informe a quantidade.' }).positive('Informe um valor positivo.'),
  horario: z.string().min(1, 'Informe a data e hora.'),
  custo: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
})

export type AlimentacaoFormValues = z.infer<typeof alimentacaoSchema>

import { z } from 'zod'

export const mortalidadeSchema = z.object({
  loteId: z.string().uuid('Selecione um lote.'),
  quantidade: z.coerce.number().int('Informe um número inteiro.').positive('Informe a quantidade.'),
  data: z.string().min(1, 'Informe a data.'),
  motivo: z.string().min(2, 'Informe o motivo.').max(200),
  observacoes: z.string().optional(),
})

export type MortalidadeFormValues = z.infer<typeof mortalidadeSchema>

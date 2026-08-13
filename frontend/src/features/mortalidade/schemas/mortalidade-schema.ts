import { z } from 'zod'

export const mortalidadeSchema = z.object({
  loteId: z.string().uuid('Selecione um lote.'),
  quantidade: z.coerce.number().int('Informe um número inteiro.').positive('Informe a quantidade.'),
  data: z.string().min(1, 'Informe a data.'),
  causa: z.enum(['RETIRADA_ABATE', 'TRANSFERENCIA', 'MORTE'], {
    errorMap: () => ({ message: 'Selecione a causa.' }),
  }),
  observacoes: z.string().optional(),
})

export type MortalidadeFormValues = z.infer<typeof mortalidadeSchema>

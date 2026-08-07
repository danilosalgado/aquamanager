import { z } from 'zod'

export const crescimentoSchema = z.object({
  loteId: z.string().uuid('Selecione um lote.'),
  pesoMedioG: z.coerce.number().positive('Informe um peso médio válido.'),
  quantidadeAmostra: z.coerce
    .number()
    .int('Informe um número inteiro.')
    .positive('Informe a quantidade da amostra.'),
  dataPesagem: z.string().min(1, 'Informe a data da pesagem.'),
})

export type CrescimentoFormValues = z.infer<typeof crescimentoSchema>

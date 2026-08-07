import { z } from 'zod'

const optionalNumber = z.coerce.number().optional().or(z.literal('').transform(() => undefined))

export const qualidadeAguaSchema = z.object({
  tanqueId: z.string().uuid('Selecione um tanque.'),
  medidoEm: z.string().min(1, 'Informe a data e hora da medição.'),
  temperatura: optionalNumber,
  ph: optionalNumber,
  oxigenioDissolvido: optionalNumber,
  amonia: optionalNumber,
  nitrito: optionalNumber,
  alcalinidade: optionalNumber,
  salinidade: optionalNumber,
  transparenciaCm: optionalNumber,
})

export type QualidadeAguaFormValues = z.infer<typeof qualidadeAguaSchema>

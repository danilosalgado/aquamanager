import { z } from 'zod'

export const especieSchema = z.object({
  nome: z.string().min(2, 'Informe o nome da espécie.').max(100),
  nomeCientifico: z.string().optional(),
  cicloDiasPadrao: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
  pesoAbatePadraoG: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
  tempMin: z.coerce.number().optional().or(z.literal('').transform(() => undefined)),
  tempMax: z.coerce.number().optional().or(z.literal('').transform(() => undefined)),
  phMin: z.coerce.number().optional().or(z.literal('').transform(() => undefined)),
  phMax: z.coerce.number().optional().or(z.literal('').transform(() => undefined)),
  oxigenioMin: z.coerce.number().optional().or(z.literal('').transform(() => undefined)),
  amoniaMax: z.coerce.number().optional().or(z.literal('').transform(() => undefined)),
  nitritoMax: z.coerce.number().optional().or(z.literal('').transform(() => undefined)),
})

export type EspecieFormValues = z.infer<typeof especieSchema>

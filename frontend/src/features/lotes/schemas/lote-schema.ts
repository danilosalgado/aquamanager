import { z } from 'zod'

export const loteSchema = z.object({
  tanqueId: z.string().uuid('Selecione o tanque.'),
  especieId: z.string().uuid('Selecione a espécie.'),
  fornecedor: z.string().optional(),
  quantidadeInicial: z.coerce.number({ invalid_type_error: 'Informe a quantidade.' }).positive('Informe um valor positivo.'),
  pesoInicialG: z.coerce.number({ invalid_type_error: 'Informe o peso inicial.' }).positive('Informe um valor positivo.'),
  valorCompra: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
  dataCompra: z.string().min(1, 'Informe a data da compra.'),
  previsaoVenda: z.string().optional(),
  status: z.enum(['ATIVO', 'VENDIDO', 'ENCERRADO']).default('ATIVO'),
})

export type LoteFormValues = z.infer<typeof loteSchema>

export const statusLoteLabels: Record<string, string> = {
  ATIVO: 'Ativo',
  VENDIDO: 'Vendido',
  ENCERRADO: 'Encerrado',
}

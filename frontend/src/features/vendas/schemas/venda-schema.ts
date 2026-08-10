import { z } from 'zod'

export const vendaSchema = z.object({
  categoriaProduto: z.string().min(1, 'Informe a categoria do produto.').max(80),
  quantidadeKg: z.coerce.number().positive('Informe uma quantidade válida.'),
  valorTotal: z.coerce.number().positive('Informe um valor válido.'),
  dataVenda: z.string().min(1, 'Informe a data da venda.'),
  clienteId: z.string().uuid().optional().or(z.literal('').transform(() => undefined)),
  observacoes: z.string().max(500).optional().or(z.literal('').transform(() => undefined)),
})

export type VendaFormValues = z.infer<typeof vendaSchema>

export const categoriasSugeridas = [
  'Peixe Inteiro Vivo',
  'Peixe Inteiro Bruto',
  'Peixe Inteiro Tratado',
  'Filé',
]

import { z } from 'zod'

export const lancamentoSchema = z.object({
  tipo: z.enum(['RECEITA', 'DESPESA'], { required_error: 'Selecione o tipo.' }),
  categoria: z.string().min(1, 'Informe a categoria.').max(100),
  descricao: z.string().min(2, 'Informe a descrição.').max(255),
  valor: z.coerce.number().positive('Informe um valor válido.'),
  dataVencimento: z.string().min(1, 'Informe a data de vencimento.'),
  formaPagamento: z.string().optional().or(z.literal('').transform(() => undefined)),
  clienteId: z.string().uuid().optional().or(z.literal('').transform(() => undefined)),
  fornecedorId: z.string().uuid().optional().or(z.literal('').transform(() => undefined)),
  loteId: z.string().uuid().optional().or(z.literal('').transform(() => undefined)),
})

export type LancamentoFormValues = z.infer<typeof lancamentoSchema>

export const tipoLancamentoLabels: Record<string, string> = {
  RECEITA: 'Receita',
  DESPESA: 'Despesa',
}

export const statusLancamentoLabels: Record<string, string> = {
  PENDENTE: 'Pendente',
  PAGO: 'Pago',
  ATRASADO: 'Atrasado',
  CANCELADO: 'Cancelado',
}

export const formasPagamento = ['Pix', 'Boleto', 'Cartão', 'Dinheiro', 'Outro']

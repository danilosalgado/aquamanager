import { z } from 'zod'

export const itemSchema = z.object({
  categoria: z.enum(['RACAO', 'MEDICAMENTO', 'QUIMICO', 'EQUIPAMENTO', 'MATERIAL'], {
    required_error: 'Selecione a categoria.',
  }),
  nome: z.string().min(2, 'Informe o nome do item.').max(150),
  unidade: z.string().min(1, 'Informe a unidade.').max(10),
  quantidadeMinima: z.coerce.number().min(0).optional().or(z.literal('').transform(() => undefined)),
  fornecedorId: z.string().uuid().optional().or(z.literal('').transform(() => undefined)),
  validade: z.string().optional().or(z.literal('').transform(() => undefined)),
  precoUnitario: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
})

export type ItemFormValues = z.infer<typeof itemSchema>

export const movimentacaoSchema = z.object({
  itemId: z.string().uuid({ message: 'Selecione um item.' }),
  tipo: z.enum(['ENTRADA', 'SAIDA'], { required_error: 'Selecione o tipo.' }),
  quantidade: z.coerce.number().positive('Informe uma quantidade válida.'),
  motivo: z.string().optional().or(z.literal('').transform(() => undefined)),
})

export type MovimentacaoFormValues = z.infer<typeof movimentacaoSchema>

export const categoriaLabels: Record<string, string> = {
  RACAO: 'Ração',
  MEDICAMENTO: 'Medicamento',
  QUIMICO: 'Químico',
  EQUIPAMENTO: 'Equipamento',
  MATERIAL: 'Material',
}

export const tipoMovimentacaoLabels: Record<string, string> = {
  ENTRADA: 'Entrada',
  SAIDA: 'Saída',
}

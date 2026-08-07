import { z } from 'zod'

export const fornecedorSchema = z.object({
  nome: z.string().min(2, 'Informe o nome do fornecedor.').max(150),
  documento: z.string().optional(),
  telefone: z.string().optional(),
  email: z.string().email('Informe um e-mail válido.').optional().or(z.literal('')),
  produtosFornecidos: z.string().optional(),
  observacoes: z.string().optional(),
})

export type FornecedorFormValues = z.infer<typeof fornecedorSchema>

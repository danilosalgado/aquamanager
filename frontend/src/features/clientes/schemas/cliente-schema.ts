import { z } from 'zod'

export const clienteSchema = z.object({
  nome: z.string().min(2, 'Informe o nome do cliente.').max(150),
  documento: z.string().optional(),
  telefone: z.string().optional(),
  email: z.string().email('Informe um e-mail válido.').optional().or(z.literal('')),
  endereco: z.string().optional(),
  observacoes: z.string().optional(),
})

export type ClienteFormValues = z.infer<typeof clienteSchema>

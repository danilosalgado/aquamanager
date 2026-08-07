import { z } from 'zod'

export const tanqueSchema = z.object({
  nome: z.string().min(2, 'Informe o nome do tanque.').max(100),
  codigo: z.string().min(1, 'Informe um código.').max(30),
  tipo: z.enum(['ESCAVADO', 'REDE', 'CONCRETO', 'BIOFLOCO'], { required_error: 'Selecione o tipo.' }),
  areaM2: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
  volumeM3: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
  profundidadeM: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
  capacidadeEstimadaKg: z.coerce.number().positive().optional().or(z.literal('').transform(() => undefined)),
  status: z.enum(['ATIVO', 'INATIVO', 'MANUTENCAO']).default('ATIVO'),
  observacoes: z.string().optional(),
})

export type TanqueFormValues = z.infer<typeof tanqueSchema>

export const tipoTanqueLabels: Record<string, string> = {
  ESCAVADO: 'Escavado',
  REDE: 'Rede',
  CONCRETO: 'Concreto',
  BIOFLOCO: 'Biofloco',
}

export const statusTanqueLabels: Record<string, string> = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
  MANUTENCAO: 'Em manutenção',
}

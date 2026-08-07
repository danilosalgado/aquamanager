import { z } from 'zod'

const senhaSchema = z
  .string()
  .min(8, 'A senha deve ter pelo menos 8 caracteres.')
  .regex(/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Use letra maiúscula, minúscula e número.')

const roleSchema = z.enum(['ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO', 'CONSULTOR'], {
  required_error: 'Selecione o papel.',
})

export const usuarioCreateSchema = z.object({
  nome: z.string().min(2, 'Informe o nome.').max(150),
  email: z.string().email('Informe um e-mail válido.'),
  senha: senhaSchema,
  role: roleSchema,
})
export type UsuarioCreateFormValues = z.infer<typeof usuarioCreateSchema>

export const usuarioEditSchema = z.object({
  nome: z.string().min(2, 'Informe o nome.').max(150),
  role: roleSchema,
  ativo: z.boolean(),
})
export type UsuarioEditFormValues = z.infer<typeof usuarioEditSchema>

export const roleLabels: Record<string, string> = {
  ADMINISTRADOR: 'Administrador',
  GERENTE: 'Gerente',
  FUNCIONARIO: 'Funcionário',
  CONSULTOR: 'Consultor',
}

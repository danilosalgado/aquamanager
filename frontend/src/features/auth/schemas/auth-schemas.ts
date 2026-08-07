import { z } from 'zod'

const senhaSchema = z
  .string()
  .min(8, 'A senha deve ter pelo menos 8 caracteres.')
  .regex(/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Use letra maiúscula, minúscula e número.')

export const loginSchema = z.object({
  email: z.string().email('Informe um e-mail válido.'),
  senha: z.string().min(1, 'Informe sua senha.'),
  codigo2fa: z.string().optional(),
})
export type LoginFormValues = z.infer<typeof loginSchema>

export const registerSchema = z.object({
  nomeEmpresa: z.string().min(2, 'Informe o nome da empresa.'),
  documento: z.string().min(11, 'Informe um CPF ou CNPJ válido.'),
  emailEmpresa: z.string().email('Informe um e-mail válido.'),
  telefone: z.string().optional(),
  cidade: z.string().optional(),
  estado: z.string().max(2).optional(),
  endereco: z.string().optional(),
  nomeUsuario: z.string().min(2, 'Informe seu nome.'),
  emailUsuario: z.string().email('Informe um e-mail válido.'),
  senha: senhaSchema,
})
export type RegisterFormValues = z.infer<typeof registerSchema>

export const forgotPasswordSchema = z.object({
  email: z.string().email('Informe um e-mail válido.'),
})
export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>

export const resetPasswordSchema = z.object({
  novaSenha: senhaSchema,
})
export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>

import { apiClient } from '@/lib/api-client'

export const assistenteApi = {
  perguntar: async (pergunta: string): Promise<string> => {
    const response = await apiClient.post<{ resposta: string }>('/assistente/perguntar', { pergunta })
    return response.data.resposta
  }
}

import { apiClient } from '@/lib/api-client'

export const relatoriosApi = {
  baixar: async (
    tipo: 'financeiro' | 'producao' | 'mortalidade' | 'estoque',
    formato: 'pdf' | 'excel',
    inicio?: Date,
    fim?: Date
  ) => {
    const params = new URLSearchParams()
    params.append('formato', formato)
    if (inicio) params.append('inicio', inicio.toISOString())
    if (fim) params.append('fim', fim.toISOString())

    const response = await apiClient.get(`/relatorios/${tipo}?${params.toString()}`, {
      responseType: 'blob', // Important for downloading files
    })

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    
    // Attempt to extract filename from content-disposition header if available
    const disposition = response.headers['content-disposition']
    let filename = `relatorio-${tipo}.${formato === 'pdf' ? 'pdf' : 'xlsx'}`
    if (disposition && disposition.indexOf('attachment') !== -1) {
      const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
      const matches = filenameRegex.exec(disposition)
      if (matches != null && matches[1]) {
        filename = matches[1].replace(/['"]/g, '')
      }
    }
    
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  },
}

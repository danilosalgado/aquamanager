import type { AxiosResponse } from 'axios'

/** Dispara o download de uma resposta axios com responseType 'blob', usando o nome de
 * arquivo do header Content-Disposition quando disponível. */
export function baixarBlob(response: AxiosResponse<Blob>, nomeArquivoPadrao: string) {
  const url = window.URL.createObjectURL(new Blob([response.data]))
  const link = document.createElement('a')
  link.href = url

  let filename = nomeArquivoPadrao
  const disposition = response.headers['content-disposition']
  if (disposition && disposition.indexOf('attachment') !== -1) {
    const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
    const matches = filenameRegex.exec(disposition)
    if (matches?.[1]) {
      filename = matches[1].replace(/['"]/g, '')
    }
  }

  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

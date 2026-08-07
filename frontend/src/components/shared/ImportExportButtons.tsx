import { useRef, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { Download, Upload, FileDown, CheckCircle2, XCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { extractErrorMessage } from '@/lib/api-client'
import type { ImportResultado } from '@/types/api'

interface ImportExportButtonsProps {
  entidadeLabel: string
  onDownloadTemplate: () => Promise<void>
  onExport: () => Promise<void>
  onImport: (file: File) => Promise<ImportResultado>
  onImportComplete?: () => void
}

/** Botões reutilizáveis de "Baixar modelo / Importar planilha / Exportar", usados nas
 * páginas de registros operacionais para permitir que produtores já com dados tabelados
 * carreguem tudo de uma vez em vez de digitar linha a linha. */
export function ImportExportButtons({
  entidadeLabel, onDownloadTemplate, onExport, onImport, onImportComplete,
}: ImportExportButtonsProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [importando, setImportando] = useState(false)
  const [exportando, setExportando] = useState(false)
  const [baixandoModelo, setBaixandoModelo] = useState(false)
  const [resultado, setResultado] = useState<ImportResultado | null>(null)

  const handleFileSelected = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setImportando(true)
    try {
      const res = await onImport(file)
      setResultado(res)
      if (res.importados > 0) {
        onImportComplete?.()
      }
    } catch (error) {
      toast.error(extractErrorMessage(error, 'Não foi possível importar a planilha.'))
    } finally {
      setImportando(false)
    }
  }

  const handleExport = async () => {
    setExportando(true)
    try {
      await onExport()
    } catch (error) {
      toast.error(extractErrorMessage(error, 'Não foi possível exportar os dados.'))
    } finally {
      setExportando(false)
    }
  }

  const handleTemplate = async () => {
    setBaixandoModelo(true)
    try {
      await onDownloadTemplate()
    } catch (error) {
      toast.error(extractErrorMessage(error, 'Não foi possível baixar o modelo.'))
    } finally {
      setBaixandoModelo(false)
    }
  }

  return (
    <>
      <div className="flex flex-wrap items-center gap-2">
        <Button variant="outline" size="sm" onClick={handleTemplate} loading={baixandoModelo}>
          <FileDown className="h-4 w-4" /> Baixar modelo
        </Button>
        <Button variant="outline" size="sm" onClick={() => fileInputRef.current?.click()} loading={importando}>
          <Upload className="h-4 w-4" /> Importar planilha
        </Button>
        <Button variant="outline" size="sm" onClick={handleExport} loading={exportando}>
          <Download className="h-4 w-4" /> Exportar
        </Button>
        <input ref={fileInputRef} type="file" accept=".xlsx" className="hidden" onChange={handleFileSelected} />
      </div>

      <Dialog open={!!resultado} onOpenChange={(open) => !open && setResultado(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Resultado da importação</DialogTitle>
            <DialogDescription>
              {entidadeLabel} — {resultado?.totalLinhas ?? 0} linha(s) encontrada(s) na planilha.
            </DialogDescription>
          </DialogHeader>

          {resultado && (
            <div className="space-y-4">
              <div className="flex gap-3">
                <Badge variant="success" className="gap-1.5">
                  <CheckCircle2 className="h-3.5 w-3.5" /> {resultado.importados} importado(s)
                </Badge>
                {resultado.erros.length > 0 && (
                  <Badge variant="destructive" className="gap-1.5">
                    <XCircle className="h-3.5 w-3.5" /> {resultado.erros.length} com erro
                  </Badge>
                )}
              </div>

              {resultado.erros.length > 0 && (
                <div className="max-h-64 overflow-y-auto rounded-lg border border-border">
                  <table className="w-full text-sm">
                    <thead className="bg-secondary text-secondary-foreground">
                      <tr>
                        <th className="px-3 py-2 text-left font-medium">Linha</th>
                        <th className="px-3 py-2 text-left font-medium">Motivo</th>
                      </tr>
                    </thead>
                    <tbody>
                      {resultado.erros.map((erro) => (
                        <tr key={erro.linha} className="border-t border-border">
                          <td className="px-3 py-2 align-top text-muted-foreground">{erro.linha}</td>
                          <td className="px-3 py-2">{erro.motivo}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          <DialogFooter>
            <Button onClick={() => setResultado(null)}>Fechar</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

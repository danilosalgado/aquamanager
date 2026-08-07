import { useState, useRef, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Bot, User, Send, X, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { assistenteApi } from '../api/assistente-api'
import { extractErrorCode, extractErrorMessage } from '@/lib/api-client'
import { cn } from '@/lib/utils'

interface Mensagem {
  id: string
  role: 'user' | 'assistant'
  content: string
  upgradeNecessario?: boolean
}

interface AssistenteChatProps {
  open: boolean
  onClose: () => void
}

export function AssistenteChat({ open, onClose }: AssistenteChatProps) {
  const [mensagens, setMensagens] = useState<Mensagem[]>([
    { id: '1', role: 'assistant', content: 'Olá! Sou seu assistente AquaManager. Como posso ajudar com a sua produção hoje?' }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (bottomRef.current) {
      bottomRef.current.scrollIntoView({ behavior: 'smooth' })
    }
  }, [mensagens])

  if (!open) return null

  const handleSend = async () => {
    if (!input.trim() || loading) return
    const userMsg: Mensagem = { id: Date.now().toString(), role: 'user', content: input.trim() }
    setMensagens(prev => [...prev, userMsg])
    setInput('')
    setLoading(true)

    try {
      const resposta = await assistenteApi.perguntar(userMsg.content)
      setMensagens(prev => [...prev, { id: Date.now().toString(), role: 'assistant', content: resposta }])
    } catch (e) {
      const upgradeNecessario = extractErrorCode(e) === 'FEATURE_NOT_AVAILABLE'
      const content = extractErrorMessage(e, 'Desculpe, ocorreu um erro ao consultar o assistente.')
      setMensagens(prev => [...prev, { id: Date.now().toString(), role: 'assistant', content, upgradeNecessario }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-y-0 right-0 w-full sm:w-96 bg-card border-l border-border shadow-2xl flex flex-col z-50 animate-in slide-in-from-right duration-300">
      <div className="flex items-center justify-between p-4 border-b border-border bg-muted/30">
        <div className="flex items-center gap-2">
          <Bot className="h-5 w-5 text-primary" />
          <h2 className="font-semibold text-foreground">Assistente AquaManager</h2>
        </div>
        <Button variant="ghost" size="icon" onClick={onClose} className="h-8 w-8 rounded-full">
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {mensagens.map(msg => (
          <div key={msg.id} className={cn("flex flex-col max-w-[85%]", msg.role === 'user' ? "ml-auto items-end" : "mr-auto items-start")}>
            <div className="flex items-center gap-2 mb-1">
              {msg.role === 'user' ? (
                <>
                  <span className="text-xs text-muted-foreground">Você</span>
                  <User className="h-3 w-3 text-muted-foreground" />
                </>
              ) : (
                <>
                  <Bot className="h-3 w-3 text-primary" />
                  <span className="text-xs text-muted-foreground font-medium text-primary">AquaIA</span>
                </>
              )}
            </div>
            <div className={cn(
              "px-3 py-2 rounded-2xl text-sm whitespace-pre-wrap",
              msg.role === 'user' ? "bg-primary text-primary-foreground rounded-tr-sm" : "bg-muted text-foreground rounded-tl-sm border border-border/50"
            )}>
              {msg.content}
            </div>
            {msg.upgradeNecessario && (
              <Button asChild size="sm" className="mt-2">
                <Link to="/configuracoes" onClick={onClose}>Ver planos</Link>
              </Button>
            )}
          </div>
        ))}
        {loading && (
          <div className="flex items-center gap-2 text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            <span className="text-xs">Assistente digitando...</span>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="p-4 border-t border-border bg-background">
        <form onSubmit={e => { e.preventDefault(); handleSend(); }} className="flex items-center gap-2">
          <Input
            value={input}
            onChange={e => setInput(e.target.value)}
            placeholder="Pergunte sobre seus tanques..."
            className="rounded-full"
            disabled={loading}
          />
          <Button type="submit" size="icon" disabled={!input.trim() || loading} className="rounded-full flex-shrink-0">
            <Send className="h-4 w-4" />
          </Button>
        </form>
      </div>
    </div>
  )
}

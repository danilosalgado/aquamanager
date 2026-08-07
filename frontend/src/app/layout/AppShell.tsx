import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'
import { AssistenteChat } from '@/features/assistente/components/AssistenteChat'
import { useState } from 'react'
import { Bot } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'

export function AppShell() {
  const [assistenteOpen, setAssistenteOpen] = useState(false)

  return (
    <div className="flex h-dvh overflow-hidden bg-background">
      <Sidebar className="hidden lg:flex" />
      
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar />
        
        <main className="flex-1 overflow-y-auto scrollbar-thin">
          <div className="container max-w-7xl py-6">
            <Outlet />
          </div>
        </main>
      </div>

      <AssistenteChat open={assistenteOpen} onClose={() => setAssistenteOpen(false)} />

      {!assistenteOpen && (
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              aria-label="Abrir assistente de IA"
              className="fixed bottom-6 right-6 h-14 w-14 rounded-full shadow-2xl z-40 transition-transform hover:scale-110"
              onClick={() => setAssistenteOpen(true)}
            >
              <Bot className="h-6 w-6" />
            </Button>
          </TooltipTrigger>
          <TooltipContent side="left">Assistente de IA</TooltipContent>
        </Tooltip>
      )}
    </div>
  )
}

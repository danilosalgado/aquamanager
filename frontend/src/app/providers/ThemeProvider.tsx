import { useEffect } from 'react'
import { applyThemeClass, useUiStore } from '@/stores/ui-store'

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const theme = useUiStore((s) => s.theme)

  useEffect(() => {
    applyThemeClass(theme)
    if (theme !== 'system') return
    const media = window.matchMedia('(prefers-color-scheme: dark)')
    const listener = () => applyThemeClass('system')
    media.addEventListener('change', listener)
    return () => media.removeEventListener('change', listener)
  }, [theme])

  return <>{children}</>
}
